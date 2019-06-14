package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.runner.TestHelper;
import cucumber.runtime.model.CucumberFeature;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static cucumber.runner.TestHelper.feature;
import static cucumber.runner.TestHelper.result;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TimelineFormatterTest {

    private static final Comparator<TimelineFormatter.TestData> TEST_DATA_COMPARATOR = new Comparator<TimelineFormatter.TestData>() {
        @Override
        public int compare(TimelineFormatter.TestData o1, TimelineFormatter.TestData o2) {
            return o1.id.compareTo(o2.id);
        }
    };

    private static final String REPORT_TEMPLATE_RESOURCE_DIR = "src/main/resources/io/cucumber/formatter/timeline";
    private static final String REPORT_JS = "report.js";
    private static final long STEP_DURATION_MS = 1000;

    private final Gson gson = new GsonBuilder().create();
    private final Map<String, Result> stepsToResult = new HashMap<>();
    private final Map<String, String> stepsToLocation = new HashMap<>();

    private final CucumberFeature failingFeature = feature("some/path/failing.feature", "" +
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

    private final CucumberFeature successfulFeature = feature("some/path/successful.feature", "" +
        "Feature: Successful Feature\n" +
        "  Background:\n" +
        "    Given bg_1\n" +
        "    When bg_2\n" +
        "    Then bg_3\n" +
        "  @TagB @TagC\n" +
        "  Scenario: Scenario 1\n" +
        "    Given step_10\n" +
        "    When step_20\n" +
        "    Then step_30");

    private final CucumberFeature pendingFeature = feature("some/path/pending.feature", "" +
        "Feature: Pending Feature\n" +
        "  Background:\n" +
        "    Given bg_1\n" +
        "    When bg_2\n" +
        "    Then bg_3\n" +
        "  Scenario: Scenario 1\n" +
        "    Given step_10\n" +
        "    When step_20\n" +
        "    Then step_50");

    private File reportDir;
    private File reportJsFile;

    @Before
    public void setUp() throws IOException {
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
    public void shouldWriteAllRequiredFilesToOutputDirectory() throws IOException {
        runFormatterWithPlugin();

        assertTrue(REPORT_JS + ": did not exist in output dir", reportJsFile.exists());

        final List<String> files = Arrays.asList("index.html", "formatter.js", "jquery-3.3.1.min.js", "vis.min.css", "vis.min.js", "vis.override.css");
        for (final String e : files) {
            final File actualFile = new File(reportDir, e);
            assertTrue(e + ": did not exist in output dir", actualFile.exists());
            final String actual = readFileContents(actualFile.getAbsolutePath());
            final String expected = readFileContents(new File(REPORT_TEMPLATE_RESOURCE_DIR, e).getAbsolutePath());
            assertEquals(e + " differs", expected, actual);
        }
    }

    @Test
    public void shouldWriteItemsCorrectlyToReportJsWhenRunInParallel() throws Throwable {
        TestHelper.builder()
            .withFeatures(failingFeature, successfulFeature, pendingFeature)
            .withRuntimeArgs("--plugin", "timeline:" + reportDir.getAbsolutePath(), "--threads", "3")
            .withStepsToResult(stepsToResult)
            .withStepsToLocation(stepsToLocation)
            .withTimeServiceIncrement(TimeUnit.MILLISECONDS.toNanos(STEP_DURATION_MS))
            .build()
            .run();

        final TimelineFormatter.TestData[] expectedTests = getExpectedTestData(0L); // Have to ignore actual thread id and just check not null

        final ActualReportOutput actualOutput = readReport();

        //Cannot verify size / contents of Groups as multi threading not guaranteed in Travis CI
        assertThat(actualOutput.groups).isNotEmpty();
        for (int i = 0; i < actualOutput.groups.size(); i++) {
            final TimelineFormatter.GroupData actual = actualOutput.groups.get(i);
            assertTrue(String.format("id on group %s, was not as expected", i), actual.id > 0);
            assertNotNull(String.format("content on group %s, was not as expected", i), actual.content);
        }

        //Sort the tests, output order is not a problem but obviously asserting it is
        Collections.sort(actualOutput.tests, TEST_DATA_COMPARATOR);
        assertTimelineTestDataIsAsExpected(expectedTests, actualOutput.tests, false, false);
    }

    @Test
    public void shouldWriteItemsAndGroupsCorrectlyToReportJs() throws Throwable {
        runFormatterWithPlugin();

        assertTrue(REPORT_JS + " was not found", reportJsFile.exists());

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
        Collections.sort(actualOutput.tests, TEST_DATA_COMPARATOR);

        assertTimelineTestDataIsAsExpected(expectedTests, actualOutput.tests, true, true);
        assertTimelineGroupDataIsAsExpected(expectedGroups, actualOutput.groups);
    }

    private TimelineFormatter.TestData[] getExpectedTestData(Long groupId) {
        String expectedJson = ("[\n" +
            "  {\n" +
            "    \"id\": \"failing-feature;scenario-1\",\n" +
            "    \"feature\": \"Failing Feature\",\n" +
            "    \"start\": 0,\n" +
            "    \"end\": 6000,\n" +
            "    \"group\": groupId,\n" +
            "    \"content\": \"\",\n" +
            "    \"tags\": \"@taga,\",\n" +
            "    \"className\": \"failed\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"failing-feature;scenario-2\",\n" +
            "    \"feature\": \"Failing Feature\",\n" +
            "    \"start\": 6000,\n" +
            "    \"end\": 12000,\n" +
            "    \"group\": groupId,\n" +
            "    \"content\": \"\",\n" +
            "    \"tags\": \"\",\n" +
            "    \"className\": \"failed\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"pending-feature;scenario-1\",\n" +
            "    \"feature\": \"Pending Feature\",\n" +
            "    \"start\": 12000,\n" +
            "    \"end\": 18000,\n" +
            "    \"group\": groupId,\n" +
            "    \"content\": \"\",\n" +
            "    \"tags\": \"\",\n" +
            "    \"className\": \"undefined\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"successful-feature;scenario-1\",\n" +
            "    \"feature\": \"Successful Feature\",\n" +
            "    \"start\": 18000,\n" +
            "    \"end\": 24000,\n" +
            "    \"group\": groupId,\n" +
            "    \"content\": \"\",\n" +
            "    \"tags\": \"@tagb,@tagc,\",\n" +
            "    \"className\": \"passed\"\n" +
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
            .withTimeServiceIncrement(STEP_DURATION_MS)
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
            }
            else if (line.startsWith("CucumberHTML.timelineGroups")) {
                addTo = groupLines;
            }
            else if (!line.startsWith("]") && !line.startsWith("$") && !line.startsWith("}")) {
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
        assertEquals("Number of tests was not as expected", expectedTests.length, actualOutput.size());
        for (int i = 0; i < expectedTests.length; i++) {
            final TimelineFormatter.TestData expected = expectedTests[i];
            final TimelineFormatter.TestData actual = actualOutput.get(i);

            assertEquals(String.format("id on item %s, was not as expected", i), expected.id, actual.id);
            assertEquals(String.format("feature on item %s, was not as expected", i), expected.feature, actual.feature);
            assertEquals(String.format("className on item %s, was not as expected", i), expected.className, actual.className);
            assertEquals(String.format("content on item %s, was not as expected", i), expected.content, actual.content);
            assertEquals(String.format("tags on item %s, was not as expected", i), expected.tags, actual.tags);
            if (checkActualTimeStamps) {
                assertEquals(String.format("startTime on item %s, was not as expected", i), expected.startTime, actual.startTime);
                assertEquals(String.format("endTime on item %s, was not as expected", i), expected.endTime, actual.endTime);
            } else {
                assertNotNull(String.format("startTime on item %s, was not as expected", i), actual.startTime);
                assertNotNull(String.format("endTime on item %s, was not as expected", i), actual.endTime);
            }
            if (checkActualThreadData) {
                assertEquals(String.format("threadId on item %s, was not as expected", i), expected.threadId, actual.threadId);
            }
            else {
                assertNotNull(String.format("threadId on item %s, was not as expected", i), actual.threadId);
            }
        }
    }

    private void assertTimelineGroupDataIsAsExpected(final TimelineFormatter.GroupData[] expectedGroups,
                                                     final List<TimelineFormatter.GroupData> actualOutput) {
        assertEquals("Number of groups was not as expected", expectedGroups.length, actualOutput.size());
        for (int i = 0; i < expectedGroups.length; i++) {
            final TimelineFormatter.GroupData expected = expectedGroups[i];
            final TimelineFormatter.GroupData actual = actualOutput.get(i);

            assertEquals(String.format("id on group %s, was not as expected", i), expected.id, actual.id);
            assertEquals(String.format("content on group %s, was not as expected", i), expected.content, actual.content);
        }
    }

    private class ActualReportOutput {

        private final List<TimelineFormatter.TestData> tests;
        private final List<TimelineFormatter.GroupData> groups;

        ActualReportOutput(final TimelineFormatter.TestData[] tests, final TimelineFormatter.GroupData[] groups) {
            this.tests = Arrays.asList(tests);
            this.groups = Arrays.asList(groups);
        }
    }

}
