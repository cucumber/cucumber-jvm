package cucumber.runtime.formatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cucumber.api.Result;
import cucumber.runtime.TestHelper;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;

import static cucumber.runtime.TestHelper.result;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TimelineFormatterTest {
    
    private static final String REPORT_TEMPLATE_RESOURCE_DIR = "src/main/resources/cucumber/formatter/timeline";
    private static final String REPORT_JS = "report.js";

    private final Gson gson = new GsonBuilder().create();
    private final List<String> features = asList("cucumber/runtime/formatter/FormatterParallelTests.feature", "cucumber/runtime/formatter/FormatterParallelTests2.feature");
    private final Map<String, Result> stepsToResult = new HashMap<String, Result>();
    
    private File reportDir;
    
    @Before
    public void setUp() throws IOException {
        reportDir = TempDir.createTempDirectory();
        
        stepsToResult.put("bg_1", result("passed"));
        stepsToResult.put("bg_2", result("passed"));
        stepsToResult.put("bg_3", result("passed"));
        stepsToResult.put("step_1", result("passed"));
        stepsToResult.put("step_2", result("failed"));
        stepsToResult.put("step_3", result("failed"));
        stepsToResult.put("bg_10", result("passed"));
        stepsToResult.put("bg_20", result("passed"));
        stepsToResult.put("bg_30", result("passed"));
        stepsToResult.put("step_10", result("passed"));
        stepsToResult.put("step_20", result("passed"));
        stepsToResult.put("step_30", result("passed"));
    }
    
    @Test
    public void shouldWriteAllRequiredFilesToOutputDirectory() throws IOException {
        TestHelper.runFormatterWithPlugin("timeline", reportDir.getAbsolutePath(), features, 1, stepsToResult);
        
        assertTrue(REPORT_JS + ": did not exist in output dir", new File(reportDir, REPORT_JS).exists());

        final List<String> files = Arrays.asList("index.html", "formatter.js", "jquery-3.3.1.min.js", "vis.min.css", "vis.min.js", "vis.override.css");
        for(final String e : files) {
            final File actualFile = new File(reportDir, e);
            assertTrue(e + ": did not exist in output dir", actualFile.exists());
            final String actual = TestHelper.readFileContents(actualFile.getAbsolutePath());
            final String expected = TestHelper.readFileContents(new File(REPORT_TEMPLATE_RESOURCE_DIR, e).getAbsolutePath());
            assertEquals(e + " differs", expected, actual);
        }
    }

    @Test
    public void shouldWriteItemsAndGroupsCorrectlyToReportJs() throws Throwable {        
        TestHelper.runFormatterWithPlugin("timeline", reportDir.getAbsolutePath(), features, 1, stepsToResult);
        final File reportJsFile = new File(reportDir, REPORT_JS);
        assertTrue(REPORT_JS + " was not found", reportJsFile.exists());

        final TimelineFormatter.TestData[] expectedTests = gson.fromJson("[\n" +
            "  {\n" +
            "    \"id\": \"feature-1;scenario-1\",\n" +
            "    \"feature\": \"feature-1\",\n" +
            "    \"start\": 0,\n" +
            "    \"end\": 16042,\n" +
            "    \"group\": 0,\n" +
            "    \"content\": \"Scenario: Scenario_1\",\n" +
            "    \"className\": \"failed\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"feature-2;scenario-2\",\n" +
            "    \"feature\": \"feature-2\",\n" +
            "    \"start\": 17276,\n" +
            "    \"end\": 33318,\n" +
            "    \"group\": 0,\n" +
            "    \"content\": \"Scenario: Scenario_2\",\n" +
            "    \"className\": \"passed\"\n" +
            "  }\n" +
            "]", TimelineFormatter.TestData[].class);

        final TimelineFormatter.GroupData[] expectedGroups = gson.fromJson("[\n" +
            "  {\n" +
            "    \"id\": 0,\n" +
            "    \"content\": \"Thread 0\"\n" +
            "  }\n" +
            "]", TimelineFormatter.GroupData[].class);

        final ActualReportOutput actualOutput = runReport(reportJsFile);
        assertTimelineDataIsAsExpected(expectedTests, expectedGroups, actualOutput);
    }

    @Test
    public void shouldHandleTestsBeingRunConcurrently() throws Throwable {
        TestHelper.runFormatterWithPlugin("timeline", reportDir.getAbsolutePath(), features, features.size(), stepsToResult);

        final File reportJsFile = new File(reportDir, REPORT_JS);
        assertTrue(REPORT_JS + " was not found", reportJsFile.exists());

        final TimelineFormatter.TestData[] expectedTests = gson.fromJson("[\n" +
            "  {\n" +
            "    \"id\": \"feature-1;scenario-1\",\n" +
            "    \"feature\": \"feature-1\",\n" +
            "    \"start\": 0,\n" +
            "    \"end\": 16042,\n" +
            "    \"group\": 0,\n" +
            "    \"content\": \"Scenario: Scenario_1\",\n" +
            "    \"className\": \"failed\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": \"feature-2;scenario-2\",\n" +
            "    \"feature\": \"feature-2\",\n" +
            "    \"start\": 0,\n" +
            "    \"end\": 16042,\n" +
            "    \"group\": 0,\n" +
            "    \"content\": \"Scenario: Scenario_2\",\n" +
            "    \"className\": \"passed\"\n" +
            "  }\n" +
            "]", TimelineFormatter.TestData[].class);

        final TimelineFormatter.GroupData[] expectedGroups = gson.fromJson("[\n" +
            "  {\n" +
            "    \"id\": 0,\n" +
            "    \"content\": \"Thread 0\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": 1,\n" +
            "    \"content\": \"Thread 1\"\n" +
            "  }\n" +
            "]", TimelineFormatter.GroupData[].class);

        final ActualReportOutput actualOutput = runReport(reportJsFile);
        
        assertTimelineDataIsAsExpected(expectedTests, expectedGroups, actualOutput);;
    }

    private ActualReportOutput runReport(final File reportJsFile) throws IOException {
        final String[] actualLines = TestHelper.readFileContents(reportJsFile.getAbsolutePath()).split("\n");
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

    private void assertTimelineDataIsAsExpected(final TimelineFormatter.TestData[] expectedTests, final TimelineFormatter.GroupData[] expectedGroups, final ActualReportOutput actualOutput) {
        final List<Long> usedThreads = new ArrayList<Long>();

        assertEquals("Number of tests was not as expected", expectedTests.length, actualOutput.tests.length);
        for (int i = 0; i < expectedTests.length; i++) {
            final TimelineFormatter.TestData expected = expectedTests[i];
            final TimelineFormatter.TestData actual = actualOutput.tests[i];

            assertEquals(String.format("id on item %s, was not as expected", i), expected.id, actual.id);
            assertEquals(String.format("feature on item %s, was not as expected", i), expected.feature, actual.feature);
            assertEquals(String.format("startTime on item %s, was not as expected", i), expected.startTime, actual.startTime);
            assertEquals(String.format("endTime on item %s, was not as expected", i), expected.endTime, actual.endTime);
            assertEquals(String.format("className on item %s, was not as expected", i), expected.className, actual.className);
            assertEquals(String.format("content on item %s, was not as expected", i), expected.content, actual.content);
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
