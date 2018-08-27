package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.runner.TestHelper;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cucumber.runner.TestHelper.result;
import static org.junit.Assert.assertEquals;

public class RerunFormatterTest {

    private final List<CucumberFeature> features = new ArrayList<>();
    private final Map<String, Result> stepsToResult = new HashMap<>();
    private final List<SimpleEntry<String, Result>> hooks = new ArrayList<>();

    @Test
    public void should_leave_report_empty_when_exit_code_is_zero() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: passed scenario\n" +
                "    Given passed step\n" +
                "  Scenario: pending scenario\n" +
                "    Given pending step\n" +
                "  Scenario: undefined scenario\n" +
                "    Given undefined step\n");
        features.add(feature);
        stepsToResult.put("passed step", result("passed"));
        stepsToResult.put("pending step", result("pending"));
        stepsToResult.put("undefined step", result("undefined"));

        String formatterOutput = runFeaturesWithFormatter(false);

        assertEquals("", formatterOutput);
    }

    @Test
    public void should_put_data_in_report_when_exit_code_is_non_zero() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: failed scenario\n" +
                "    Given failed step\n" +
                "  Scenario: pending scenario\n" +
                "    Given pending step\n" +
                "  Scenario: undefined scenario\n" +
                "    Given undefined step\n");
        features.add(feature);
        stepsToResult.put("failed step", result("failed"));
        stepsToResult.put("pending step", result("pending"));
        stepsToResult.put("undefined step", result("undefined"));

        String formatterOutput = runFeaturesWithFormatter(true);

        assertEquals("path/test.feature:2:4:6\n", formatterOutput);
    }

    @Test
    public void should_use_scenario_location_when_scenario_step_fails() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("failed"));

        String formatterOutput = runFeaturesWithFormatter(false);

        assertEquals("path/test.feature:2\n", formatterOutput);
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
        features.add(feature);
        stepsToResult.put("background step", result("failed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));

        String formatterOutput = runFeaturesWithFormatter(false);

        assertEquals("path/test.feature:4\n", formatterOutput);
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
        features.add(feature);
        stepsToResult.put("executing first row", result("passed"));
        stepsToResult.put("executing second row", result("failed"));
        stepsToResult.put("everything is ok", result("passed"));

        String formatterOutput = runFeaturesWithFormatter(false);

        assertEquals("path/test.feature:8\n", formatterOutput);
    }

    @Test
    public void should_use_scenario_location_when_before_hook_fails() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        hooks.add(TestHelper.hookEntry("before", result("failed")));

        String formatterOutput = runFeaturesWithFormatter(false);

        assertEquals("path/test.feature:2\n", formatterOutput);
    }

    @Test
    public void should_use_scenario_location_when_after_hook_fails() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));
        hooks.add(TestHelper.hookEntry("after", result("failed")));

        String formatterOutput = runFeaturesWithFormatter(false);

        assertEquals("path/test.feature:2\n", formatterOutput);
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
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("failed"));
        stepsToResult.put("third step", result("failed"));
        stepsToResult.put("forth step", result("passed"));

        String formatterOutput = runFeaturesWithFormatter(false);

        assertEquals("path/test.feature:2:5\n", formatterOutput);
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
        features.add(feature1);
        features.add(feature2);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("failed"));
        stepsToResult.put("third step", result("failed"));
        stepsToResult.put("forth step", result("passed"));

        String formatterOutput = runFeaturesWithFormatter(false);

        assertEquals("path/second.feature:2\npath/first.feature:2\n", formatterOutput);
    }

    private String runFeaturesWithFormatter(boolean isStrict) {
        final StringBuffer report = new StringBuffer();
        final RerunFormatter formatter = new RerunFormatter(report);
        formatter.setStrict(isStrict);

        TestHelper.builder()
            .withFormatterUnderTest(formatter)
            .withFeatures(features)
            .withStepsToResult(stepsToResult)
            .withHooks(hooks)
            .withTimeServiceIncrement(0L)
            .build()
            .run();

        return report.toString();
    }

}
