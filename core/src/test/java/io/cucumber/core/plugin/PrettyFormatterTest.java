package io.cucumber.core.plugin;

import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runner.TestHelper;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.plugin.event.Result;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.cucumber.core.plugin.BytesContainsString.bytesContainsString;
import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static io.cucumber.core.runner.TestDefinitionArgument.createArguments;
import static io.cucumber.core.runner.TestHelper.createWriteHookAction;
import static io.cucumber.core.runner.TestHelper.result;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

class PrettyFormatterTest {

    private final List<Feature> features = new ArrayList<>();
    private final Map<String, Result> stepsToResult = new HashMap<>();
    private final Map<String, String> stepsToLocation = new HashMap<>();
    private final List<SimpleEntry<String, Result>> hooks = new ArrayList<>();
    private final List<String> hookLocations = new ArrayList<>();
    private final List<Answer<Object>> hookActions = new ArrayList<>();

    @Test
    void should_align_the_indentation_of_location_strings() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n" +
            "    When second step\n" +
            "    Then third step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToLocation.put("second step", "path/step_definitions.java:7");
        stepsToLocation.put("third step", "path/step_definitions.java:11");

        assertThat(runFeaturesWithFormatter(true), isBytesEqualTo("" +
            "\n" +
            "Scenario: scenario name # path/test.feature:2\n" +
            "  Given first step      # path/step_definitions.java:3\n" +
            "  When second step      # path/step_definitions.java:7\n" +
            "  Then third step       # path/step_definitions.java:11\n"));
    }

    @Test
    void should_handle_background() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background name\n" +
            "    Given first step\n" +
            "  Scenario: s1\n" +
            "    Then second step\n" +
            "  Scenario: s2\n" +
            "    Then third step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToLocation.put("second step", "path/step_definitions.java:7");
        stepsToLocation.put("third step", "path/step_definitions.java:11");

        assertThat(runFeaturesWithFormatter(true), bytesContainsString("" +
            "\n" +
            "Scenario: s1       # path/test.feature:4\n" +
            "  Given first step # path/step_definitions.java:3\n" +
            "  Then second step # path/step_definitions.java:7\n" +
            "\n" +
            "Scenario: s2       # path/test.feature:6\n" +
            "  Given first step # path/step_definitions.java:3\n" +
            "  Then third step  # path/step_definitions.java:11\n"));
    }

    @Test
    void should_handle_scenario_outline() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario Outline: <name>\n" +
            "    Given first step\n" +
            "    Then <arg> step\n" +
            "    Examples: examples name\n" +
            "      |  name  |  arg   |\n" +
            "      | name 1 | second |\n" +
            "      | name 2 | third  |\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToLocation.put("second step", "path/step_definitions.java:7");
        stepsToLocation.put("third step", "path/step_definitions.java:11");

        assertThat(runFeaturesWithFormatter(true), bytesContainsString("" +
            "\n" +
            "Scenario Outline: name 1 # path/test.feature:7\n" +
            "  Given first step       # path/step_definitions.java:3\n" +
            "  Then second step       # path/step_definitions.java:7\n" +
            "\n" +
            "Scenario Outline: name 2 # path/test.feature:8\n" +
            "  Given first step       # path/step_definitions.java:3\n" +
            "  Then third step        # path/step_definitions.java:11\n"));
    }

    @Test
    void should_print_tags() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "@feature_tag\n" +
            "Feature: feature name\n" +
            "  @scenario_tag\n" +
            "  Scenario: scenario name\n" +
            "    Then second step\n" +
            "  @scenario_outline_tag\n" +
            "  Scenario Outline: scenario outline name\n" +
            "    Then <arg> step\n" +
            "    @examples_tag\n" +
            "    Examples: examples name\n" +
            "      |  arg   |\n" +
            "      | third  |\n");
        features.add(feature);
        stepsToLocation.put("second step", "path/step_definitions.java:7");
        stepsToLocation.put("third step", "path/step_definitions.java:11");

        assertThat(runFeaturesWithFormatter(true), isBytesEqualTo("" +

            "\n" +
            "@feature_tag @scenario_tag\n" +
            "Scenario: scenario name # path/test.feature:4\n" +
            "  Then second step      # path/step_definitions.java:7\n" +
            "\n" +
            "@feature_tag @scenario_outline_tag @examples_tag\n" +
            "Scenario Outline: scenario outline name # path/test.feature:12\n" +
            "  Then third step                       # path/step_definitions.java:11\n"));
    }

    @Test
    void should_print_error_message_for_failed_steps() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToResult.put("first step", result("failed"));

        assertThat(runFeaturesWithFormatter(true), bytesContainsString("" +
            "  Given first step      # path/step_definitions.java:3\n" +
            "      the stack trace\n"));
    }

    @Test
    void should_print_error_message_for_before_hooks() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToResult.put("first step", result("passed"));
        hooks.add(TestHelper.hookEntry("before", result("failed")));
        hookLocations.add("hook-location");

        assertThat(runFeaturesWithFormatter(true), bytesContainsString("" +
            "Scenario: scenario name # path/test.feature:2\n" +
            "      the stack trace\n" +
            "  Given first step      # path/step_definitions.java:3\n"));
    }

    @Test
    void should_print_error_message_for_after_hooks() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToResult.put("first step", result("passed"));
        hooks.add(TestHelper.hookEntry("after", result("failed")));
        hookLocations.add("hook-location");

        assertThat(runFeaturesWithFormatter(true), bytesContainsString("" +
            "  Given first step      # path/step_definitions.java:3\n" +
            "      the stack trace\n"));
    }

    @Test
    void should_print_output_from_before_hooks() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToResult.put("first step", result("passed"));
        hooks.add(TestHelper.hookEntry("before", result("passed")));
        hookLocations.add("hook-location");
        hookActions.add(createWriteHookAction("printed from hook"));

        assertThat(runFeaturesWithFormatter(true), bytesContainsString("" +
            "Scenario: scenario name # path/test.feature:2\n" +
            "\n" +
            "    printed from hook\n" +
            "\n" +
            "  Given first step      # path/step_definitions.java:3\n"));
    }

    @Test
    void should_print_output_from_after_hooks() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToResult.put("first step", result("passed"));
        hooks.add(TestHelper.hookEntry("after", result("passed")));
        hookLocations.add("hook-location");
        hookActions.add(createWriteHookAction("printed from hook"));

        assertThat(runFeaturesWithFormatter(true), bytesContainsString("" +
            "  Given first step      # path/step_definitions.java:3\n" +
            "\n" +
            "    printed from hook\n"));
    }

    @Test
    void should_print_output_from_afterStep_hooks() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n" +
            "    When second step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToLocation.put("second step", "path/step_definitions.java:4");
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        hooks.add(TestHelper.hookEntry("afterstep", result("passed")));
        hookLocations.add("hook-location");
        hookActions.add(createWriteHookAction("printed from afterstep hook"));

        assertThat(runFeaturesWithFormatter(true), bytesContainsString("" +
            "  Given first step      # path/step_definitions.java:3\n" +
            "\n" +
            "    printed from afterstep hook\n" +
            "\n" +
            "  When second step      # path/step_definitions.java:4\n" +
            "\n" +
            "    printed from afterstep hook" +
            "\n"));
    }

    @Test
    void should_color_code_steps_according_to_the_result() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToResult.put("first step", result("passed"));

        assertThat(runFeaturesWithFormatter(false), bytesContainsString("" +
            "  " + AnsiEscapes.GREEN + "Given " + AnsiEscapes.RESET + AnsiEscapes.GREEN + "first step" + AnsiEscapes.RESET));
    }

    @Test
    void should_color_code_locations_as_comments() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToResult.put("first step", result("passed"));

        assertThat(runFeaturesWithFormatter(false), bytesContainsString("" +
            AnsiEscapes.GREY + "# path/step_definitions.java:3" + AnsiEscapes.RESET + "\n"));
    }

    @Test
    void should_color_code_error_message_according_to_the_result() throws IOException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToResult.put("first step", result("failed"));

        assertThat(runFeaturesWithFormatter(false), bytesContainsString("" +
            "      " + AnsiEscapes.RED + "the stack trace" + AnsiEscapes.RESET + "\n"));
    }

    @Test
    void should_mark_subsequent_arguments_in_steps() throws IOException {
        Formats formats = new AnsiFormats();

        StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry);
        StepExpression expression = stepExpressionFactory.createExpression("text {string} text {string}");

        PrettyFormatter prettyFormatter = new PrettyFormatter(new ByteArrayOutputStream());
        String stepText = "text 'arg1' text 'arg2'";
        String formattedText = prettyFormatter.formatStepText("Given ", stepText, formats.get("passed"), formats.get("passed_arg"), createArguments(expression.match(stepText)));

        assertThat(formattedText, equalTo(AnsiEscapes.GREEN + "Given " + AnsiEscapes.RESET +
            AnsiEscapes.GREEN + "text " + AnsiEscapes.RESET +
            AnsiEscapes.GREEN + AnsiEscapes.INTENSITY_BOLD + "'arg1'" + AnsiEscapes.RESET +
            AnsiEscapes.GREEN + " text " + AnsiEscapes.RESET +
            AnsiEscapes.GREEN + AnsiEscapes.INTENSITY_BOLD + "'arg2'" + AnsiEscapes.RESET));
    }

    @Test
    void should_mark_nested_argument_as_part_of_full_argument() throws IOException {
        Formats formats = new AnsiFormats();

        StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry);
        StepExpression expression = stepExpressionFactory.createExpression("^the order is placed( and (not yet )?confirmed)?$");

        PrettyFormatter prettyFormatter = new PrettyFormatter(new ByteArrayOutputStream());
        String stepText = "the order is placed and not yet confirmed";

        String formattedText = prettyFormatter.formatStepText("Given ", stepText, formats.get("passed"), formats.get("passed_arg"), createArguments(expression.match(stepText)));

        assertThat(formattedText, equalTo(AnsiEscapes.GREEN + "Given " + AnsiEscapes.RESET +
            AnsiEscapes.GREEN + "the order is placed" + AnsiEscapes.RESET +
            AnsiEscapes.GREEN + AnsiEscapes.INTENSITY_BOLD + " and not yet confirmed" + AnsiEscapes.RESET));
    }

    @Test
    void should_mark_nested_arguments_as_part_of_enclosing_argument() throws IOException {
        Formats formats = new AnsiFormats();
        PrettyFormatter prettyFormatter = new PrettyFormatter(new ByteArrayOutputStream());
        StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry);
        StepExpression expression = stepExpressionFactory.createExpression("^the order is placed( and (not( yet)? )?confirmed)?$");
        String stepText = "the order is placed and not yet confirmed";
        String formattedText = prettyFormatter.formatStepText("Given ", stepText, formats.get("passed"), formats.get("passed_arg"), createArguments(expression.match(stepText)));


        assertThat(formattedText, equalTo(AnsiEscapes.GREEN + "Given " + AnsiEscapes.RESET +
            AnsiEscapes.GREEN + "the order is placed" + AnsiEscapes.RESET +
            AnsiEscapes.GREEN + AnsiEscapes.INTENSITY_BOLD + " and not yet confirmed" + AnsiEscapes.RESET));
    }

    private ByteArrayOutputStream runFeaturesWithFormatter(boolean monochrome) throws IOException {
        final ByteArrayOutputStream report = new ByteArrayOutputStream();
        final PrettyFormatter formatter = new PrettyFormatter(report);
        formatter.setMonochrome(monochrome);

        TestHelper.builder()
            .withFormatterUnderTest(formatter)
            .withFeatures(features)
            .withStepsToResult(stepsToResult)
            .withStepsToLocation(stepsToLocation)
            .withHooks(hooks)
            .withHookLocations(hookLocations)
            .withHookActions(hookActions)
            .build()
            .run();

        return report;
    }

}
