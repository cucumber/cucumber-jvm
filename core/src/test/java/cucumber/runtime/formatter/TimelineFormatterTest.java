package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.runtime.TestHelper;
import cucumber.runtime.model.CucumberFeature;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static cucumber.runtime.TestHelper.feature;
import static cucumber.runtime.TestHelper.result;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TimelineFormatterTest {

    private static final String REPORT_TEMPLATE_RESOURCE_DIR = "src/main/resources/cucumber/formatter/timeline";
    private static final String REPORT_JS = "report.js";
    private static final long STEP_DURATION = 1000;

    private final Gson gson = new GsonBuilder().create();
    private final Map<String, Result> stepsToResult = new HashMap<String, Result>();
    private final Map<String, String> stepsToLocation = new HashMap<String, String>();

    private final CucumberFeature failingFeature = feature("some\\windows\\path\\failing.feature", "" +
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

    private final CucumberFeature successfulFeature = feature("some\\windows\\path\\successful.feature", "" +
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

    private final CucumberFeature pendingFeature = feature("some\\windows\\path\\pending.feature", "" +
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

        stepsToResult.put("bg_1", result("passed"));
        stepsToResult.put("bg_2", result("passed"));
        stepsToResult.put("bg_3", result("passed"));
        stepsToResult.put("step_01", result("passed"));
        stepsToResult.put("step_02", result("passed"));
        stepsToResult.put("step_03", result("failed"));
        stepsToResult.put("step_10", result("passed"));
        stepsToResult.put("step_20", result("passed"));
        stepsToResult.put("step_30", result("passed"));

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
    public void shouldWriteItemsAndGroupsCorrectlyToReportJs() throws Throwable {
        runFormatterWithPlugin();

        assertTrue(REPORT_JS + " was not found", reportJsFile.exists());

        final TimelineFormatter.TestData[] expectedTests = gson.fromJson("[\n" +
            "  {\n" +
            "    \"id\": \"failing-feature;scenario-1\",\n" +
            "    \"feature\": \"Failing Feature\",\n" +
            "    \"start\": 0,\n" +
            "    \"end\": 1000,\n" +
            "    \"group\": 0,\n" +
            "    \"content\": \"Failing Feature<br/>Scenario 1\",\n" +
            "    \"tags\": \"@taga,\",\n" +
            "    \"className\": \"failed\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"failing-feature;scenario-2\",\n" +
            "    \"feature\": \"Failing Feature\",\n" +
            "    \"start\": 0,\n" +
            "    \"end\": 1000,\n" +
            "    \"group\": 0,\n" +
            "    \"content\": \"Failing Feature<br/>Scenario 2\",\n" +
            "    \"tags\": \"\",\n" +
            "    \"className\": \"failed\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"pending-feature;scenario-1\",\n" +
            "    \"feature\": \"Pending Feature\",\n" +
            "    \"start\": 0,\n" +
            "    \"end\": 1000,\n" +
            "    \"group\": 0,\n" +
            "    \"content\": \"Pending Feature<br/>Scenario 1\",\n" +
            "    \"tags\": \"\",\n" +
            "    \"className\": \"undefined\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"successful-feature;scenario-1\",\n" +
            "    \"feature\": \"Successful Feature\",\n" +
            "    \"start\": 0,\n" +
            "    \"end\": 1000,\n" +
            "    \"group\": 0,\n" +
            "    \"content\": \"Successful Feature<br/>Scenario 1\",\n" +
            "    \"tags\": \"@tagb,@tagc,\",\n" +
            "    \"className\": \"passed\"\n" +
            "  }\n" +
            "]", TimelineFormatter.TestData[].class);

        final TimelineFormatter.GroupData[] expectedGroups = gson.fromJson("[\n" +
            "  {\n" +
            "    \"id\": 0,\n" +
            "    \"content\": \"Thread 0\"\n" +
            "  }\n" +
            "]", TimelineFormatter.GroupData[].class);

        final ActualReportOutput actualOutput = readReport();
        assertTimelineDataIsAsExpected(expectedTests, expectedGroups, actualOutput);
    }

    private void runFormatterWithPlugin() {
        try {
            TestHelper.runFeaturesWithFormatter(Arrays.asList(failingFeature, successfulFeature, pendingFeature), stepsToResult, stepsToLocation,
                Collections.<AbstractMap.SimpleEntry<String, Result>>emptyList(), Collections.<String>emptyList(),
                Collections.<Answer<Object>>emptyList(), STEP_DURATION, null, "--plugin", "timeline:" + reportDir.getAbsolutePath());
        }
        catch (final Throwable e) {
            throw new RuntimeException(e);
        }
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

    private void assertTimelineDataIsAsExpected(final TimelineFormatter.TestData[] expectedTests, final TimelineFormatter.GroupData[] expectedGroups, final ActualReportOutput actualOutput) {
        final List<Long> usedThreads = new ArrayList<Long>();

        assertEquals("Number of tests was not as expected", expectedTests.length, actualOutput.tests.length);
        for (int i = 0; i < expectedTests.length; i++) {
            final TimelineFormatter.TestData expected = expectedTests[i];
            final TimelineFormatter.TestData actual = actualOutput.tests[i];

            assertEquals(String.format("id on item %s, was not as expected", i), expected.id, actual.id);
            assertEquals(String.format("feature on item %s, was not as expected", i), expected.feature, actual.feature);
            assertNotNull(String.format("startTime on item %s, was not as expected", i), actual.startTime);
            assertNotNull(String.format("endTime on item %s, was not as expected", i), actual.endTime);
            assertEquals(String.format("className on item %s, was not as expected", i), expected.className, actual.className);
            assertEquals(String.format("content on item %s, was not as expected", i), expected.content, actual.content);
            assertEquals(String.format("tags on item %s, was not as expected", i), expected.tags, actual.tags);
            assertNotNull(String.format("threadId on item %s, was not as expected", i), actual.threadId);
            usedThreads.add(actual.threadId);
        }

        assertEquals("Number of groups was not as expected", expectedGroups.length, actualOutput.groups.length);
        for (int i = 0; i < actualOutput.groups.length; i++) {
            final TimelineFormatter.GroupData actual = actualOutput.groups[i];
            assertTrue(String.format("Group Thread Id [%s] was not in Used Thread Ids [%s]", actual.id, usedThreads), usedThreads.contains(actual.id));
        }
    }

    private class ActualReportOutput {

        private final TimelineFormatter.TestData[] tests;
        private final TimelineFormatter.GroupData[] groups;

        ActualReportOutput(final TimelineFormatter.TestData[] tests, final TimelineFormatter.GroupData[] groups) {
            this.tests = tests;
            this.groups = groups;
        }
    }

}
