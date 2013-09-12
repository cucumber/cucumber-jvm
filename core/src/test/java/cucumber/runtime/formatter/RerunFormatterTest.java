package cucumber.runtime.formatter;

import cucumber.runtime.TestHelper;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RerunFormatterTest {

    @Test
    public void should_leave_report_empty_when_no_scenario_fails() throws Throwable {
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

        String formatterOutput = runFeatureWithRerunFormatter(feature, stepsToResult);

        assertEquals("", formatterOutput);
    }

    @Test
    public void should_use_scenario_location_when_scenario_step_fails() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        Map<String, String> stepsToResult = new HashMap<String, String>();
        stepsToResult.put("first step", "passed");
        stepsToResult.put("second step", "passed");
        stepsToResult.put("third step", "failed");

        String formatterOutput = runFeatureWithRerunFormatter(feature, stepsToResult);

        assertEquals("path/test.feature:2", formatterOutput);
    }

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

    @Test
    public void should_one_entry_for_feature_with_many_failing_scenarios() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario 1 name\n" +
                "    When first step\n" +
                "    Then second step\n" +
                "  Scenario: scenario 2 name\n" +
                "    When third step\n" +
                "    Then forth step\n");
        Map<String, String> stepsToResult = new HashMap<String, String>();
        stepsToResult.put("first step", "passed");
        stepsToResult.put("second step", "failed");
        stepsToResult.put("third step", "failed");
        stepsToResult.put("forth step", "passed");

        String formatterOutput = runFeatureWithRerunFormatter(feature, stepsToResult);

        assertEquals("path/test.feature:2:5", formatterOutput);
    }

    @Test
    public void should_one_entry_for_each_failing_feature() throws Throwable {
        CucumberFeature feature1 = TestHelper.feature("path/first.feature", "" +
                "Feature: feature 1 name\n" +
                "  Scenario: scenario 1 name\n" +
                "    When first step\n" +
                "    Then second step\n");
        CucumberFeature feature2 = TestHelper.feature("path/second.feature", "" +
                "Feature: feature 2 name\n" +
                "  Scenario: scenario 2 name\n" +
                "    When third step\n" +
                "    Then forth step\n");
        Map<String, String> stepsToResult = new HashMap<String, String>();
        stepsToResult.put("first step", "passed");
        stepsToResult.put("second step", "failed");
        stepsToResult.put("third step", "failed");
        stepsToResult.put("forth step", "passed");

        String formatterOutput = runFeaturesWithRerunFormatter(Arrays.asList(feature1, feature2), stepsToResult);

        assertEquals("path/second.feature:2 path/first.feature:2", formatterOutput);
    }

    private String runFeatureWithRerunFormatter(final CucumberFeature feature, final Map<String, String> stepsToResult)
            throws Throwable {
        return runFeatureWithRerunFormatter(feature, stepsToResult, Collections.<SimpleEntry<String, String>>emptyList());
    }

    private String runFeatureWithRerunFormatter(final CucumberFeature feature, final Map<String, String> stepsToResult,
                                                final List<SimpleEntry<String, String>> hooks) throws Throwable {
        return runFeaturesWithRerunFormatter(Arrays.asList(feature), stepsToResult, hooks);
    }

    private String runFeaturesWithRerunFormatter(final List<CucumberFeature> features, final Map<String, String> stepsToResult)
            throws Throwable {
        return runFeaturesWithRerunFormatter(features, stepsToResult, Collections.<SimpleEntry<String, String>>emptyList());
    }

    private String runFeaturesWithRerunFormatter(final List<CucumberFeature> features, final Map<String, String> stepsToResult,
            final List<SimpleEntry<String, String>> hooks) throws Throwable {
        final StringBuffer buffer = new StringBuffer();
        final RerunFormatter rerunFormatter = new RerunFormatter(buffer);
        final long stepHookDuration = 0;
        TestHelper.runFeaturesWithFormatter(features, stepsToResult, hooks, stepHookDuration, rerunFormatter, rerunFormatter);
        return buffer.toString();
    }

}
