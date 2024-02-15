package io.cucumber.core.plugin;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.TimelineFormatter.GroupData;
import io.cucumber.core.plugin.TimelineFormatter.TestData;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimelineFormatterTest {

    private static final Comparator<TestData> TEST_DATA_COMPARATOR = Comparator
            .comparing(TestData::getScenario);

    private static final Path REPORT_TEMPLATE_RESOURCE_DIR = Paths
            .get("src/main/resources/io/cucumber/core/plugin/timeline");

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(Include.NON_NULL)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

    // private final Gson gson = new GsonBuilder().registerTypeAdapter(
    // Instant.class,
    // (JsonDeserializer<Instant>) (json, type, jsonDeserializationContext) ->
    // json.isJsonObject()
    // ?
    // Instant.ofEpochSecond(json.getAsJsonObject().get("seconds").getAsLong())
    // : Instant.ofEpochMilli(json.getAsLong()))
    // .create();

    private final Feature failingFeature = TestFeatureParser.parse("some/path/failing.feature", "" +
            "Feature: Failing Feature\n" +
            " Background:\n" +
            " Given bg_1\n" +
            " When bg_2\n" +
            " Then bg_3\n" +
            " @TagA\n" +
            " Scenario: Scenario 1\n" +
            " Given step_01\n" +
            " When step_02\n" +
            " Then step_03\n" +
            " Scenario: Scenario 2\n" +
            " Given step_01\n" +
            " When step_02\n" +
            " Then step_03");

    private final Feature successfulFeature = TestFeatureParser.parse("some/path/successful.feature", "" +
            "Feature: Successful Feature\n" +
            " Background:\n" +
            " Given bg_1\n" +
            " When bg_2\n" +
            " Then bg_3\n" +
            " @TagB @TagC\n" +
            " Scenario: Scenario 3\n" +
            " Given step_10\n" +
            " When step_20\n" +
            " Then step_30");

    private final Feature pendingFeature = TestFeatureParser.parse("some/path/pending.feature", "" +
            "Feature: Pending Feature\n" +
            " Background:\n" +
            " Given bg_1\n" +
            " When bg_2\n" +
            " Then bg_3\n" +
            " Scenario: Scenario 4\n" +
            " Given step_10\n" +
            " When step_20\n" +
            " Then step_50");

    @TempDir
    Path reportDir;
    Path reportJsFile;
    RuntimeOptionsBuilder runtimeOptionsBuilder;

    @BeforeEach
    void setUp() {
        reportJsFile = reportDir.resolve("report.js");
        runtimeOptionsBuilder = new RuntimeOptionsBuilder()
                .addPluginName("timeline:" + reportDir.toAbsolutePath());
    }

    @Test
    void shouldWriteAllRequiredFilesToOutputDirectory() throws IOException {
        runFormatterWithPlugin();

        assertThat(Files.exists(reportJsFile), is(equalTo(true)));

        List<String> files = Arrays.asList("index.html", "formatter.js",
            "jquery-3.5.1.min.js",
            "vis-timeline-graph2d.min.css", "vis-timeline-graph2d.min.js",
            "vis-timeline-graph2d.override.css");
        for (String resource : files) {
            Path actualFile = reportDir.resolve(resource);
            assertTrue(Files.exists(actualFile), resource + ": did not exist in output dir");
            String actual = readFileContents(actualFile);
            String expected = readFileContents(REPORT_TEMPLATE_RESOURCE_DIR.resolve(resource));
            assertThat(resource + " differs", actual, is(equalTo(expected)));
        }
    }

    private void runFormatterWithPlugin() {
        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofMillis(1000));

        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(failingFeature,
                    successfulFeature, pendingFeature))
                .withAdditionalPlugins(timeService)
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("bg_1"),
                    new StubStepDefinition("bg_2"),
                    new StubStepDefinition("bg_3"),
                    new StubStepDefinition("step_01"),
                    new StubStepDefinition("step_02"),
                    new StubStepDefinition("step_03", new StubException()),
                    new StubStepDefinition("step_10"),
                    new StubStepDefinition("step_20"),
                    new StubStepDefinition("step_30")))
                .withRuntimeOptions(runtimeOptionsBuilder.build())
                .build()
                .run();
    }

    private String readFileContents(Path outputPath) throws IOException {
        return String.join("\n", Files.readAllLines(outputPath));
    }

    @Test
    void shouldWriteItemsCorrectlyToReportJsWhenRunInParallel() throws Throwable {
        runtimeOptionsBuilder.setThreads(2);
        runFormatterWithPlugin();

        // Have to ignore actual thread id and just checknot null
        final TestData[] expectedTests = getExpectedTestData(0L);

        final ActualReportOutput actualOutput = readReport();

        // Cannot verify size / contents of Groups as multi threading not
        // guaranteed in Travis CI
        assertThat(actualOutput.groups, not(empty()));
        for (int i = 0; i < actualOutput.groups.size(); i++) {
            final GroupData actual = actualOutput.groups.get(i);

            final int idx = i;
            assertAll(
                () -> assertThat(String.format("id on group %s, was not as expected", idx),
                    actual.getId() > 0,
                    is(equalTo(true))),
                () -> assertThat(String.format("content on group %s, was not as expected",
                    idx), actual.getContent(),
                    is(notNullValue())));
        }

        // Sort the tests, output order is not a problem but obviously asserting
        // it is
        actualOutput.tests.sort(TEST_DATA_COMPARATOR);
        assertTimelineTestDataIsAsExpected(expectedTests, actualOutput.tests, false,
            false);
    }

    private TestData[] getExpectedTestData(Long groupId) throws JsonProcessingException {
        String expectedJson = ("[\n" +
                " {\n" +
                " \"feature\": \"Failing Feature\",\n" +
                " \"scenario\": \"Scenario 1\",\n" +
                " \"start\": 0,\n" +
                " \"end\": 6000,\n" +
                " \"group\": groupId,\n" +
                " \"content\": \"\",\n" +
                " \"tags\": \"@taga,\",\n" +
                " \"className\": \"failed\"\n" +
                " },\n" +
                " {\n" +
                " \"feature\": \"Failing Feature\",\n" +
                " \"scenario\": \"Scenario 2\",\n" +
                " \"start\": 6000,\n" +
                " \"end\": 12000,\n" +
                " \"group\": groupId,\n" +
                " \"content\": \"\",\n" +
                " \"tags\": \"\",\n" +
                " \"className\": \"failed\"\n" +
                " },\n" +
                " {\n" +
                " \"feature\": \"Successful Feature\",\n" +
                " \"scenario\": \"Scenario 3\",\n" +
                " \"start\": 18000,\n" +
                " \"end\": 24000,\n" +
                " \"group\": groupId,\n" +
                " \"content\": \"\",\n" +
                " \"tags\": \"@tagb,@tagc,\",\n" +
                " \"className\": \"passed\"\n" +
                " },\n" +
                " {\n" +
                " \"scenario\": \"Scenario 4\",\n" +
                " \"feature\": \"Pending Feature\",\n" +
                " \"start\": 12000,\n" +
                " \"end\": 18000,\n" +
                " \"group\": groupId,\n" +
                " \"content\": \"\",\n" +
                " \"tags\": \"\",\n" +
                " \"className\": \"undefined\"\n" +
                " }\n" +
                "]").replaceAll("groupId", groupId.toString());

        return objectMapper.readValue(expectedJson, TestData[].class);
    }

    private ActualReportOutput readReport() throws IOException {
        String itemLines = "";
        String groupLines = "";

        for (String line : Files.readAllLines(reportJsFile)) {
            if (line.startsWith("CucumberHTML.timelineItems.pushArray(")) {
                itemLines = line.substring("CucumberHTML.timelineItems.pushArray(".length(), line.length() - 2);
            } else if (line.startsWith("CucumberHTML.timelineGroups")) {
                groupLines = line.substring("CucumberHTML.timelineGroups.pushArray(".length(), line.length() - 2);
            }
        }
        TestData[] tests = objectMapper.readValue(itemLines, TestData[].class);
        GroupData[] groups = objectMapper.readValue(groupLines, GroupData[].class);
        return new ActualReportOutput(tests, groups);
    }

    private void assertTimelineTestDataIsAsExpected(
            final TestData[] expectedTests,
            final List<TestData> actualOutput,
            final boolean checkActualThreadData,
            final boolean checkActualTimeStamps
    ) {
        assertThat("Number of tests was not as expected", actualOutput.size(),
            is(equalTo(expectedTests.length)));
        for (int i = 0; i < expectedTests.length; i++) {
            final TestData expected = expectedTests[i];
            final TestData actual = actualOutput.get(i);
            final int idx = i;

            assertAll(
                () -> assertThat(String.format("feature on item %s, was not as expected", idx),
                    actual.getFeature(),
                    is(equalTo(expected.getFeature()))),
                () -> assertThat(String.format("className on item %s, was not as expected", idx),
                    actual.getClassName(),
                    is(equalTo(expected.getClassName()))),
                () -> assertThat(String.format("content on item %s, was not as expected", idx),
                    actual.getContent(),
                    is(equalTo(expected.getContent()))),
                () -> assertThat(String.format("tags on item %s, was not as expected", idx),
                    actual.getTags(),
                    is(equalTo(expected.getTags()))),
                () -> {
                    if (checkActualTimeStamps) {
                        assertAll(
                            () -> assertThat(String.format("startTime on item %s, was not as expected", idx),
                                actual.getStart(), is(equalTo(expected.getStart()))),
                            () -> assertThat(String.format("endTime on item %s, was not as expected", idx),
                                actual.getEnd(), is(equalTo(expected.getEnd()))));
                    } else {
                        assertAll(
                            () -> assertThat(String.format("startTime on item %s, was not as expected", idx),
                                actual.getStart(), is(notNullValue())),
                            () -> assertThat(String.format("endTime on item %s, was not as expected", idx),
                                actual.getEnd(), is(notNullValue())));
                    }
                },
                () -> {
                    if (checkActualThreadData) {
                        assertThat(String.format("threadId on item %s, was not as expected", idx),
                            actual.getGroup(),
                            is(equalTo(expected.getGroup())));
                    } else {
                        assertThat(String.format("threadId on item %s, was not as expected", idx),
                            actual.getGroup(),
                            is(notNullValue()));
                    }
                });
        }
    }

    @Test
    void shouldWriteItemsAndGroupsCorrectlyToReportJs() throws Throwable {
        runFormatterWithPlugin();

        assertTrue(Files.exists(reportJsFile));

        Long groupId = Thread.currentThread().getId();
        String groupName = Thread.currentThread().toString();

        TestData[] expectedTests = getExpectedTestData(groupId);

        GroupData[] expectedGroups = objectMapper.readValue(
            ("[\n" +
                    " {\n" +
                    " \"id\": groupId,\n" +
                    " \"content\": \"groupName\"\n" +
                    " }\n" +
                    "]")
                    .replaceAll("groupId", groupId.toString())
                    .replaceAll("groupName", groupName),
            GroupData[].class);

        ActualReportOutput actualOutput = readReport();

        // Sort the tests, output order is not a problem but obviously asserting
        // it is
        actualOutput.tests.sort(TEST_DATA_COMPARATOR);

        assertAll(
            () -> assertTimelineTestDataIsAsExpected(expectedTests, actualOutput.tests,
                true, true),
            () -> assertTimelineGroupDataIsAsExpected(expectedGroups,
                actualOutput.groups));
    }

    private void assertTimelineGroupDataIsAsExpected(
            GroupData[] expectedGroups,
            List<GroupData> actualOutput
    ) {
        assertThat("Number of groups was not as expected", actualOutput.size(),
            is(equalTo(expectedGroups.length)));
        for (int i = 0; i < expectedGroups.length; i++) {
            GroupData expected = expectedGroups[i];
            GroupData actual = actualOutput.get(i);

            int idx = i;
            assertAll(
                () -> assertThat(String.format("id on group %s, was not as expected", idx),
                    actual.getId(),
                    is(equalTo(expected.getId()))),
                () -> assertThat(String.format("content on group %s, was not as expected",
                    idx), actual.getContent(),
                    is(equalTo(expected.getContent()))));
        }
    }

    private static class ActualReportOutput {

        private final List<TestData> tests;
        private final List<GroupData> groups;

        ActualReportOutput(TestData[] tests, GroupData[] groups) {
            this.tests = Arrays.asList(tests);
            this.groups = Arrays.asList(groups);
        }

    }

}
