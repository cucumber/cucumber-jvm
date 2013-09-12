package cucumber.runtime.formatter;

import cucumber.runtime.TestHelper;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RerunFormatterTest {

    @Test
    public void should_use_scenario_location_when_background_step_fails() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Background: the background\n" +
                "    Given background step\n" +
                "  Scenario: scenario name\n" +
                "    When second step\n" +
                "    Then third step\n");
        Map<String, String> stepsToResult = new HashMap<String, String>();
        stepsToResult.put("background step", "failed");
        stepsToResult.put("second step", "passed");
        stepsToResult.put("third step", "passed");

        String formatterOutput = runFeatureWithRerunFormatter(feature, stepsToResult);

        assertEquals("path/test.feature:4", formatterOutput);
    }

    @Test
    public void should_use_example_row_location_when_scenario_outline_fails() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario Outline: scenario name\n" +
                "    When executing <row> row\n" +
                "    Then everything is ok\n" +
                "    Examples:\n" +
                "    |  row   |\n" +
                "    | first  |\n" +
                "    | second |");
        Map<String, String> stepsToResult = new HashMap<String, String>();
        stepsToResult.put("executing first row", "passed");
        stepsToResult.put("executing second row", "failed");
        stepsToResult.put("everything is ok", "passed");

        String formatterOutput = runFeatureWithRerunFormatter(feature, stepsToResult);

        assertEquals("path/test.feature:8", formatterOutput);
    }

    @Test
    public void should_use_scenario_location_when_before_hook_fails() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        Map<String, String> stepsToResult = new HashMap<String, String>();
        stepsToResult.put("first step", "passed");
        stepsToResult.put("second step", "passed");
        stepsToResult.put("third step", "passed");
        List<SimpleEntry<String, String>> hooks = new ArrayList<SimpleEntry<String, String>>();
        hooks.add(TestHelper.hookEntry("before", "failed"));

        String formatterOutput = runFeatureWithRerunFormatter(feature, stepsToResult, hooks);

        assertEquals("path/test.feature:2", formatterOutput);
    }

    @Test
    public void should_use_scenario_location_when_after_hook_fails() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        Map<String, String> stepsToResult = new HashMap<String, String>();
        stepsToResult.put("first step", "passed");
        stepsToResult.put("second step", "passed");
        stepsToResult.put("third step", "passed");
        List<SimpleEntry<String, String>> hooks = new ArrayList<SimpleEntry<String, String>>();
        hooks.add(TestHelper.hookEntry("after", "failed"));

        String formatterOutput = runFeatureWithRerunFormatter(feature, stepsToResult, hooks);

        assertEquals("path/test.feature:2", formatterOutput);
    }

    private String runFeatureWithRerunFormatter(final CucumberFeature feature, final Map<String, String> stepsToResult)
            throws Throwable {
        return runFeatureWithRerunFormatter(feature, stepsToResult, Collections.<SimpleEntry<String, String>>emptyList());
    }

    private String runFeatureWithRerunFormatter(final CucumberFeature feature, final Map<String, String> stepsToResult,
                                                final List<SimpleEntry<String, String>> hooks) throws Throwable {
        final StringBuffer buffer = new StringBuffer();
        final RerunFormatter rerunFormatter = new RerunFormatter(buffer);
        final long stepHookDuration = 0;
        TestHelper.runFeatureWithFormatter(feature, stepsToResult, hooks, stepHookDuration, rerunFormatter, rerunFormatter);
        return buffer.toString();
    }

}
