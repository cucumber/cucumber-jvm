package io.cucumber.core.plugin;

import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.deps.com.google.gson.JsonDeserializer;
import io.cucumber.plugin.event.Result;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.runner.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static io.cucumber.core.runner.TestHelper.result;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class TimelineFormatterTest {

    private static final Comparator<TimelineFormatter.TestData> TEST_DATA_COMPARATOR = Comparator.comparing(o -> o.scenario);

    private static final String REPORT_TEMPLATE_RESOURCE_DIR = "src/main/resources/io/cucumber/core/plugin/timeline";
    private static final String REPORT_JS = "report.js";
    private static final Duration STEP_DURATION = Duration.ofMillis(1000);

    private final Gson gson = new GsonBuilder().registerTypeAdapter(
        Instant.class,
        (JsonDeserializer<Instant>) (json, type, jsonDeserializationContext) ->
            json.isJsonObject()
                ? Instant.ofEpochSecond(json.getAsJsonObject().get("seconds").getAsLong())
                : Instant.ofEpochMilli(json.getAsLong())).create();

    private final Map<String, Result> stepsToResult = new HashMap<>();
    private final Map<String, String> stepsToLocation = new HashMap<>();

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

    private File reportDir;
    private File reportJsFile;

    @BeforeEach
    void setUp() throws IOException {
        reportDir = TempDir.createTempDirectory();
        reportJsFile = new File(reportDir, REPORT_JS);

        stepsToResult.put("step_03", result("failed"));
        stepsToResult.put("step_50", result("undefined"));

        stepsToLocation.put("bg_1", "path/step_definitions.java:3");
        stepsToLocation.put("bg_2", "path/step_definitions.java:4");
        stepsToLocation.put("bg_3", "path/step_definitions.java:5");
        stepsToLocation.put("step_01", "path/step_definitions.java:7");
        stepsToLocation.put("step_02", "path/step_definitions.java:8");
        stepsToLocation.put("step_03", "path/step_definitions.java:9");
        stepsToLocation.put("step_10", "path/step_definitions.java:7");
        stepsToLocation.put("step_20", "path/step_definitions.java:8");
        stepsToLocation.put("step_30", "path/step_definitions.java:9");
    }

    @Test
    void shouldWriteAllRequiredFilesToOutputDirectory() throws IOException {
        runFormatterWithPlugin();

        assertThat(REPORT_JS + ": did not exist in output dir", reportJsFile.exists(), is(equalTo(true)));

        final List<String> files = Arrays.asList("index.html", "formatter.js", "jquery-3.4.1.min.js", "vis.min.css", "vis.min.js", "vis.override.css");
        for (final String e : files) {
            final File actualFile = new File(reportDir, e);
            assertThat(e + ": did not exist in output dir", actualFile.exists(), is(equalTo(true)));
            final String actual = readFileContents(actualFile.getAbsolutePath());
            final String expected = readFileContents(new File(REPORT_TEMPLATE_RESOURCE_DIR, e).getAbsolutePath());
            assertThat(e + " differs", actual, is(equalTo(expected)));
        }
    }

    @Test
    void shouldWriteItemsCorrectlyToReportJsWhenRunInParallel() throws Throwable {
        TestHelper.builder()
            .withFeatures(failingFeature, successfulFeature, pendingFeature)
            .withRuntimeArgs("--plugin", "timeline:" + reportDir.getAbsolutePath(), "--threads", "3")
            .withStepsToResult(stepsToResult)
            .withStepsToLocation(stepsToLocation)
            .withTimeServiceIncrement(STEP_DURATION)
            .build()
            .run();

        final TimelineFormatter.TestData[] expectedTests = getExpectedTestData(0L); // Have to ignore actual thread id and just check not null

        final ActualReportOutput actualOutput = readReport();

        //Cannot verify size / contents of Groups as multi threading not guaranteed in Travis CI
        assertThat(actualOutput.groups, not(empty()));
        for (int i = 0; i < actualOutput.groups.size(); i++) {
            final TimelineFormatter.GroupData actual = actualOutput.groups.get(i);

            final int idx = i;
            assertAll("Checking TimelineFormatter.GroupData",
                () -> assertThat(String.format("id on group %s, was not as expected", idx), actual.id > 0, is(equalTo(true))),
                () -> assertThat(String.format("content on group %s, was not as expected", idx), actual.content, is(notNullValue()))
            );
        }

        //Sort the tests, output order is not a problem but obviously asserting it is
        actualOutput.tests.sort(TEST_DATA_COMPARATOR);
        assertTimelineTestDataIsAsExpected(expectedTests, actualOutput.tests, false, false);
    }

    @Test
    void shouldWriteItemsAndGroupsCorrectlyToReportJs() throws Throwable {
        runFormatterWithPlugin();

        assertThat(REPORT_JS + " was not found", reportJsFile.exists(), is(equalTo(true)));

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
                .replaceAll("groupName", groupName)
            , TimelineFormatter.GroupData[].class);

        final ActualReportOutput actualOutput = readReport();

        //Sort the tests, output order is not a problem but obviously asserting it is
        actualOutput.tests.sort(TEST_DATA_COMPARATOR);

        assertAll("Checking Timeline",
            () -> assertTimelineTestDataIsAsExpected(expectedTests, actualOutput.tests, true, true),
            () -> assertTimelineGroupDataIsAsExpected(expectedGroups, actualOutput.groups)
        );
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

    private void runFormatterWithPlugin() {
        TestHelper.builder()
            .withFeatures(failingFeature, successfulFeature, pendingFeature)
            .withRuntimeArgs("--plugin", "timeline:" + reportDir.getAbsolutePath())
            .withStepsToResult(stepsToResult)
            .withStepsToLocation(stepsToLocation)
            .withTimeServiceIncrement(STEP_DURATION)
            .build()
            .run();
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

        final TimelineFormatter.TestData[] tests = gson.fromJson(itemLines.toString(), TimelineFormatter.TestData[].class);
        final TimelineFormatter.GroupData[] groups = gson.fromJson(groupLines.toString(), TimelineFormatter.GroupData[].class);
        return new ActualReportOutput(tests, groups);
    }

    private String readFileContents(final String outputPath) throws IOException {
        final Scanner scanner = new Scanner(new FileInputStream(outputPath), "UTF-8");
        final String contents = scanner.useDelimiter("\\A").next();
        scanner.close();
        return contents;
    }

    private void assertTimelineTestDataIsAsExpected(final TimelineFormatter.TestData[] expectedTests,
                                                    final List<TimelineFormatter.TestData> actualOutput,
                                                    final boolean checkActualThreadData,
                                                    final boolean checkActualTimeStamps) {
        assertThat("Number of tests was not as expected", actualOutput.size(), is(equalTo(expectedTests.length)));
        for (int i = 0; i < expectedTests.length; i++) {
            final TimelineFormatter.TestData expected = expectedTests[i];
            final TimelineFormatter.TestData actual = actualOutput.get(i);
            final int idx = i;

            assertAll("Checking TimelineFormatter.TestData",
                () -> assertThat(String.format("feature on item %s, was not as expected", idx), actual.feature, is(equalTo(expected.feature))),
                () -> assertThat(String.format("className on item %s, was not as expected", idx), actual.className, is(equalTo(expected.className))),
                () -> assertThat(String.format("content on item %s, was not as expected", idx), actual.content, is(equalTo(expected.content))),
                () -> assertThat(String.format("tags on item %s, was not as expected", idx), actual.tags, is(equalTo(expected.tags))),
                () -> {
                    if (checkActualTimeStamps) {
                        assertAll("Checking ActualTimeStamps",
                            () -> assertThat(String.format("startTime on item %s, was not as expected", idx), actual.startTime, is(equalTo(expected.startTime))),
                            () -> assertThat(String.format("endTime on item %s, was not as expected", idx), actual.endTime, is(equalTo(expected.endTime)))
                        );
                    } else {
                        assertAll("Checking TimeStamps",
                            () -> assertThat(String.format("startTime on item %s, was not as expected", idx), actual.startTime, is(notNullValue())),
                            () -> assertThat(String.format("endTime on item %s, was not as expected", idx), actual.endTime, is(notNullValue()))
                        );
                    }
                },
                () -> {
                    if (checkActualThreadData) {
                        assertThat(String.format("threadId on item %s, was not as expected", idx), actual.threadId, is(equalTo(expected.threadId)));
                    } else {
                        assertThat(String.format("threadId on item %s, was not as expected", idx), actual.threadId, is(notNullValue()));
                    }
                }
            );
        }
    }

    private void assertTimelineGroupDataIsAsExpected(final TimelineFormatter.GroupData[] expectedGroups,
                                                     final List<TimelineFormatter.GroupData> actualOutput) {
        assertThat("Number of groups was not as expected", actualOutput.size(), is(equalTo(expectedGroups.length)));
        for (int i = 0; i < expectedGroups.length; i++) {
            final TimelineFormatter.GroupData expected = expectedGroups[i];
            final TimelineFormatter.GroupData actual = actualOutput.get(i);

            final int idx = i;
            assertAll("Checking TimelineFormatter.GroupData",
                () -> assertThat(String.format("id on group %s, was not as expected", idx), actual.id, is(equalTo(expected.id))),
                () -> assertThat(String.format("content on group %s, was not as expected", idx), actual.content, is(equalTo(expected.content)))
            );
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
