package io.cucumber.core.plugin;

import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runner.TestHelper;
import io.cucumber.plugin.event.Result;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static io.cucumber.core.runner.TestHelper.result;
import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;

class RerunFormatterTest {

    private final List<Feature> features = new ArrayList<>();
    private final Map<String, Result> stepsToResult = new HashMap<>();
    private final List<SimpleEntry<String, Result>> hooks = new ArrayList<>();
    private final List<String> hookLocations = new ArrayList<>();

    @Test
    void should_leave_report_empty_when_exit_code_is_zero() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: passed scenario\n" +
            "    Given passed step\n" +
            "  Scenario: skipped scenario\n" +
            "    Given skipped step\n"
        );
        features.add(feature);
        stepsToResult.put("passed step", result("passed"));
        stepsToResult.put("skipped step", result("skipped"));

        assertThat(runFeaturesWithFormatter(), isBytesEqualTo(""));
    }

    @Test
    void should_put_data_in_report_when_exit_code_is_non_zero() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
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

        assertThat(runFeaturesWithFormatter(), isBytesEqualTo("classpath:path/test.feature:2:4:6\n"));
    }

    @Test
    void should_use_scenario_location_when_scenario_step_fails() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n" +
            "    When second step\n" +
            "    Then third step\n");
        features.add(feature);
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("failed"));

        assertThat(runFeaturesWithFormatter(), isBytesEqualTo("file:path/test.feature:2\n"));
    }

    @Test
    void should_use_scenario_location_when_background_step_fails() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
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

        assertThat(runFeaturesWithFormatter(), isBytesEqualTo("file:path/test.feature:4\n"));
    }

    @Test
    void should_use_example_row_location_when_scenario_outline_fails() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
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

        assertThat(runFeaturesWithFormatter(), isBytesEqualTo("classpath:path/test.feature:8\n"));
    }

    @Test
    void should_use_scenario_location_when_before_hook_fails() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
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
        hookLocations.add("hook-location");

        assertThat(runFeaturesWithFormatter(), isBytesEqualTo("classpath:path/test.feature:2\n"));
    }

    @Test
    void should_use_scenario_location_when_after_hook_fails() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
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
        hookLocations.add("hook-location");

        assertThat(runFeaturesWithFormatter(), isBytesEqualTo("classpath:path/test.feature:2\n"));
    }

    @Test
    void should_one_entry_for_feature_with_many_failing_scenarios() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
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

        assertThat(runFeaturesWithFormatter(), isBytesEqualTo("classpath:path/test.feature:2:5\n"));
    }

    @Test
    void should_one_entry_for_each_failing_feature() {
        Feature feature1 = TestFeatureParser.parse("classpath:path/first.feature", "" +
            "Feature: feature 1 name\n" +
            "  Scenario: scenario 1 name\n" +
            "    When first step\n" +
            "    Then second step\n");
        Feature feature2 = TestFeatureParser.parse("classpath:path/second.feature", "" +
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

        assertThat(runFeaturesWithFormatter(), isBytesEqualTo("classpath:path/first.feature:2\nclasspath:path/second.feature:2\n"));
    }

    private ByteArrayOutputStream runFeaturesWithFormatter() {
        final ByteArrayOutputStream report = new ByteArrayOutputStream();
        final RerunFormatter formatter = new RerunFormatter(report);

        TestHelper.builder()
            .withFormatterUnderTest(formatter)
            .withFeatures(features)
            .withStepsToResult(stepsToResult)
            .withHooks(hooks)
            .withHookLocations(hookLocations)
            .withTimeServiceIncrement(ZERO)
            .build()
            .run();

        return report;
    }

}
