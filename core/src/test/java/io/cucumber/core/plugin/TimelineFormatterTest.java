package io.cucumber.core.plugin;

import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.internal.com.google.gson.Gson;
import io.cucumber.messages.internal.com.google.gson.GsonBuilder;
import io.cucumber.messages.internal.com.google.gson.JsonDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class TimelineFormatterTest {

    private static final Comparator<TimelineFormatter.TestData> TEST_DATA_COMPARATOR = Comparator
            .comparing(o -> o.scenario);

    private static final String REPORT_TEMPLATE_RESOURCE_DIR = "src/main/resources/io/cucumber/core/plugin/timeline";

    private final Gson gson = new GsonBuilder().registerTypeAdapter(
        Instant.class,
        (JsonDeserializer<Instant>) (json, type, jsonDeserializationContext) -> json.isJsonObject()
                ? Instant.ofEpochSecond(json.getAsJsonObject().get("seconds").getAsLong())
                : Instant.ofEpochMilli(json.getAsLong()))
            .create();

    private final Feature failingFeature = TestFeatureParser.parse("some/path/failing.feature", "" +
            "Feature: Failing Feature\n" +
            "  Background:\n" +
            "    Given bg_1\n" +
            "    When bg_2\n" +
            "    Then bg_3\n" +
            "  @TagA\n" +
            "  Scenario: Scenario 1\n" +
            "    Given step_01\n" +
            "    When step_02\n" +
            "    Then step_03\n" +
            "  Scenario: Scenario 2\n" +
            "    Given step_01\n" +
            "    When step_02\n" +
            "    Then step_03");

    private final Feature successfulFeature = TestFeatureParser.parse("some/path/successful.feature", "" +
            "Feature: Successful Feature\n" +
            "  Background:\n" +
            "    Given bg_1\n" +
            "    When bg_2\n" +
            "    Then bg_3\n" +
            "  @TagB @TagC\n" +
            "  Scenario: Scenario 3\n" +
            "    Given step_10\n" +
            "    When step_20\n" +
            "    Then step_30");

    private final Feature pendingFeature = TestFeatureParser.parse("some/path/pending.feature", "" +
            "Feature: Pending Feature\n" +
            "  Background:\n" +
            "    Given bg_1\n" +
            "    When bg_2\n" +
            "    Then bg_3\n" +
            "  Scenario: Scenario 4\n" +
            "    Given step_10\n" +
            "    When step_20\n" +
            "    Then step_50");

    @TempDir
    File reportDir;
    File reportJsFile;
    RuntimeOptionsBuilder runtimeOptionsBuilder;

    @BeforeEach
    void setUp() {
        reportJsFile = new File(reportDir, "report.js");
        runtimeOptionsBuilder = new RuntimeOptionsBuilder()
                .addPluginName("timeline:" + reportDir.getAbsolutePath());
    }

    @Test
    void shouldWriteAllRequiredFilesToOutputDirectory() throws IOException {
        runFormatterWithPlugin();

        assertThat(reportJsFile.exists(), is(equalTo(true)));

        final List<String> files = Arrays.asList("index.html", "formatter.js", "jquery-3.5.1.min.js", "vis.min.css",
            "vis.min.js", "vis.override.css");
        for (final String e : files) {
            final File actualFile = new File(reportDir, e);
            assertThat(e + ": did not exist in output dir", actualFile.exists(), is(equalTo(true)));
            final String actual = readFileContents(actualFile.getAbsolutePath());
            final String expected = readFileContents(new File(REPORT_TEMPLATE_RESOURCE_DIR, e).getAbsolutePath());
            assertThat(e + " differs", actual, is(equalTo(expected)));
        }
    }

    private void runFormatterWithPlugin() {
        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofMillis(1000));

        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(failingFeature, successfulFeature, pendingFeature))
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

    private String readFileContents(final String outputPath) throws IOException {
        final Scanner scanner = new Scanner(new FileInputStream(outputPath), "UTF-8");
        final String contents = scanner.useDelimiter("\\A").next();
        scanner.close();
        return contents;
    }

    @Test
    void shouldWriteItemsCorrectlyToReportJsWhenRunInParallel() throws Throwable {
        runtimeOptionsBuilder.setThreads(2);
        runFormatterWithPlugin();

        // Have to ignore actual thread id and just checknot null
        final TimelineFormatter.TestData[] expectedTests = getExpectedTestData(0L);

        final ActualReportOutput actualOutput = readReport();

        // Cannot verify size / contents of Groups as multi threading not
        // guaranteed in Travis CI
        assertThat(actualOutput.groups, not(empty()));
        for (int i = 0; i < actualOutput.groups.size(); i++) {
            final TimelineFormatter.GroupData actual = actualOutput.groups.get(i);

            final int idx = i;
            assertAll(
                () -> assertThat(String.format("id on group %s, was not as expected", idx), actual.id > 0,
                    is(equalTo(true))),
                () -> assertThat(String.format("content on group %s, was not as expected", idx), actual.content,
                    is(notNullValue())));
        }

        // Sort the tests, output order is not a problem but obviously asserting
        // it is
        actualOutput.tests.sort(TEST_DATA_COMPARATOR);
        assertTimelineTestDataIsAsExpected(expectedTests, actualOutput.tests, false, false);
    }

    private TimelineFormatter.TestData[] getExpectedTestData(Long groupId) {
        String expectedJson = ("[\n" +
                "  {\n" +
                "    \"feature\": \"Failing Feature\",\n" +
                "    \"scenario\": \"Scenario 1\",\n" +
                "    \"start\": 0,\n" +
                "    \"end\": 6000,\n" +
                "    \"group\": groupId,\n" +
                "    \"content\": \"\",\n" +
                "    \"tags\": \"@taga,\",\n" +
                "    \"className\": \"failed\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"feature\": \"Failing Feature\",\n" +
                "    \"scenario\": \"Scenario 2\",\n" +
                "    \"start\": 6000,\n" +
                "    \"end\": 12000,\n" +
                "    \"group\": groupId,\n" +
                "    \"content\": \"\",\n" +
                "    \"tags\": \"\",\n" +
                "    \"className\": \"failed\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"feature\": \"Successful Feature\",\n" +
                "    \"scenario\": \"Scenario 3\",\n" +
                "    \"start\": 18000,\n" +
                "    \"end\": 24000,\n" +
                "    \"group\": groupId,\n" +
                "    \"content\": \"\",\n" +
                "    \"tags\": \"@tagb,@tagc,\",\n" +
                "    \"className\": \"passed\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"scenario\": \"Scenario 4\",\n" +
                "    \"feature\": \"Pending Feature\",\n" +
                "    \"start\": 12000,\n" +
                "    \"end\": 18000,\n" +
                "    \"group\": groupId,\n" +
                "    \"content\": \"\",\n" +
                "    \"tags\": \"\",\n" +
                "    \"className\": \"undefined\"\n" +
                "  }\n" +
                "]").replaceAll("groupId", groupId.toString());

        return gson.fromJson(expectedJson, TimelineFormatter.TestData[].class);
    }

    private ActualReportOutput readReport() throws IOException {
        final String[] actualLines = readFileContents(reportJsFile.getAbsolutePath()).split("\n");
        final StringBuilder itemLines = new StringBuilder().append("[");
        final StringBuilder groupLines = new StringBuilder().append("[");
        StringBuilder addTo = null;

        for (final String line : actualLines) {
            if (line.startsWith("CucumberHTML.timelineItems")) {
                addTo = itemLines;
            } else if (line.startsWith("CucumberHTML.timelineGroups")) {
                addTo = groupLines;
            } else if (!line.startsWith("]") && !line.startsWith("$") && !line.startsWith("}")) {
                addTo.append(line);
            }
        }
        itemLines.append("]");
        groupLines.append("]");

        final TimelineFormatter.TestData[] tests = gson.fromJson(itemLines.toString(),
            TimelineFormatter.TestData[].class);
        final TimelineFormatter.GroupData[] groups = gson.fromJson(groupLines.toString(),
            TimelineFormatter.GroupData[].class);
        return new ActualReportOutput(tests, groups);
    }

    private void assertTimelineTestDataIsAsExpected(
            final TimelineFormatter.TestData[] expectedTests,
            final List<TimelineFormatter.TestData> actualOutput,
            final boolean checkActualThreadData,
            final boolean checkActualTimeStamps
    ) {
        assertThat("Number of tests was not as expected", actualOutput.size(), is(equalTo(expectedTests.length)));
        for (int i = 0; i < expectedTests.length; i++) {
            final TimelineFormatter.TestData expected = expectedTests[i];
            final TimelineFormatter.TestData actual = actualOutput.get(i);
            final int idx = i;

            assertAll(
                () -> assertThat(String.format("feature on item %s, was not as expected", idx), actual.feature,
                    is(equalTo(expected.feature))),
                () -> assertThat(String.format("className on item %s, was not as expected", idx), actual.className,
                    is(equalTo(expected.className))),
                () -> assertThat(String.format("content on item %s, was not as expected", idx), actual.content,
                    is(equalTo(expected.content))),
                () -> assertThat(String.format("tags on item %s, was not as expected", idx), actual.tags,
                    is(equalTo(expected.tags))),
                () -> {
                    if (checkActualTimeStamps) {
                        assertAll(
                            () -> assertThat(String.format("startTime on item %s, was not as expected", idx),
                                actual.startTime, is(equalTo(expected.startTime))),
                            () -> assertThat(String.format("endTime on item %s, was not as expected", idx),
                                actual.endTime, is(equalTo(expected.endTime))));
                    } else {
                        assertAll(
                            () -> assertThat(String.format("startTime on item %s, was not as expected", idx),
                                actual.startTime, is(notNullValue())),
                            () -> assertThat(String.format("endTime on item %s, was not as expected", idx),
                                actual.endTime, is(notNullValue())));
                    }
                },
                () -> {
                    if (checkActualThreadData) {
                        assertThat(String.format("threadId on item %s, was not as expected", idx), actual.threadId,
                            is(equalTo(expected.threadId)));
                    } else {
                        assertThat(String.format("threadId on item %s, was not as expected", idx), actual.threadId,
                            is(notNullValue()));
                    }
                });
        }
    }

    @Test
    void shouldWriteItemsAndGroupsCorrectlyToReportJs() throws Throwable {
        runFormatterWithPlugin();

        assertThat(reportJsFile.exists(), is(equalTo(true)));

        final Long groupId = Thread.currentThread().getId();
        final String groupName = Thread.currentThread().toString();

        final TimelineFormatter.TestData[] expectedTests = getExpectedTestData(groupId);

        final TimelineFormatter.GroupData[] expectedGroups = gson.fromJson(
            ("[\n" +
                    "  {\n" +
                    "    \"id\": groupId,\n" +
                    "    \"content\": \"groupName\"\n" +
                    "  }\n" +
                    "]")
                            .replaceAll("groupId", groupId.toString())
                            .replaceAll("groupName", groupName),
            TimelineFormatter.GroupData[].class);

        final ActualReportOutput actualOutput = readReport();

        // Sort the tests, output order is not a problem but obviously asserting
        // it is
        actualOutput.tests.sort(TEST_DATA_COMPARATOR);

        assertAll(
            () -> assertTimelineTestDataIsAsExpected(expectedTests, actualOutput.tests, true, true),
            () -> assertTimelineGroupDataIsAsExpected(expectedGroups, actualOutput.groups));
    }

    private void assertTimelineGroupDataIsAsExpected(
            final TimelineFormatter.GroupData[] expectedGroups,
            final List<TimelineFormatter.GroupData> actualOutput
    ) {
        assertThat("Number of groups was not as expected", actualOutput.size(), is(equalTo(expectedGroups.length)));
        for (int i = 0; i < expectedGroups.length; i++) {
            final TimelineFormatter.GroupData expected = expectedGroups[i];
            final TimelineFormatter.GroupData actual = actualOutput.get(i);

            final int idx = i;
            assertAll(
                () -> assertThat(String.format("id on group %s, was not as expected", idx), actual.id,
                    is(equalTo(expected.id))),
                () -> assertThat(String.format("content on group %s, was not as expected", idx), actual.content,
                    is(equalTo(expected.content))));
        }
    }

    private static class ActualReportOutput {

        private final List<TimelineFormatter.TestData> tests;
        private final List<TimelineFormatter.GroupData> groups;

        ActualReportOutput(final TimelineFormatter.TestData[] tests, final TimelineFormatter.GroupData[] groups) {
            this.tests = Arrays.asList(tests);
            this.groups = Arrays.asList(groups);
        }

    }

}
