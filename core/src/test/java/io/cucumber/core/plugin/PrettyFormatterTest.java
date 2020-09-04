package io.cucumber.core.plugin;

import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.StubHookDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.util.Locale;
import java.util.UUID;

import static io.cucumber.core.plugin.BytesContainsString.bytesContainsString;
import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static io.cucumber.core.plugin.Formats.ansi;
import static io.cucumber.core.runner.TestDefinitionArgument.createArguments;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

class PrettyFormatterTest {

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

    @Test
    void should_align_the_indentation_of_location_strings() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", "path/step_definitions.java:3"),
                    new StubStepDefinition("second step", "path/step_definitions.java:7"),
                    new StubStepDefinition("third step", "path/step_definitions.java:11")))
                .build()
                .run();

        assertThat(out, isBytesEqualTo("" +
                "\n" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "  Given first step      # path/step_definitions.java:3\n" +
                "  When second step      # path/step_definitions.java:7\n" +
                "  Then third step       # path/step_definitions.java:11\n"));
    }

    @Test
    void should_handle_background() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Background: background name\n" +
                "    Given first step\n" +
                "  Scenario: s1\n" +
                "    Then second step\n" +
                "  Scenario: s2\n" +
                "    Then third step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", "path/step_definitions.java:3"),
                    new StubStepDefinition("second step", "path/step_definitions.java:7"),
                    new StubStepDefinition("third step", "path/step_definitions.java:11")))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
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
    void should_handle_scenario_outline() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario Outline: <name>\n" +
                "    Given first step\n" +
                "    Then <arg> step\n" +
                "    Examples: examples name\n" +
                "      |  name  |  arg   |\n" +
                "      | name 1 | second |\n" +
                "      | name 2 | third  |\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", "path/step_definitions.java:3"),
                    new StubStepDefinition("second step", "path/step_definitions.java:7"),
                    new StubStepDefinition("third step", "path/step_definitions.java:11")))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
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
    void should_print_tags() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "@feature_tag\n" +
                "Feature: feature name\n" +
                "  @scenario_tag\n" +
                "  Scenario: scenario name\n" +
                "    Then first step\n" +
                "  @scenario_outline_tag\n" +
                "  Scenario Outline: scenario outline name\n" +
                "    Then <arg> step\n" +
                "    @examples_tag\n" +
                "    Examples: examples name\n" +
                "      |  arg    |\n" +
                "      | second  |\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", "path/step_definitions.java:7"),
                    new StubStepDefinition("second step", "path/step_definitions.java:11")))
                .build()
                .run();

        assertThat(out, isBytesEqualTo("" +

                "\n" +
                "@feature_tag @scenario_tag\n" +
                "Scenario: scenario name # path/test.feature:4\n" +
                "  Then first step       # path/step_definitions.java:7\n" +
                "\n" +
                "@feature_tag @scenario_outline_tag @examples_tag\n" +
                "Scenario Outline: scenario outline name # path/test.feature:12\n" +
                "  Then second step                      # path/step_definitions.java:11\n"));
    }

    @Test
    void should_print_error_message_for_failed_steps() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", "path/step_definitions.java:3", new StubException())))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "  Given first step      # path/step_definitions.java:3\n" +
                "      the stack trace\n"));
    }

    @Test
    void should_print_error_message_for_before_hooks() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition(new StubException())),
                    singletonList(new StubStepDefinition("first step", "path/step_definitions.java:3")),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "      the stack trace\n" +
                "  Given first step      # path/step_definitions.java:3\n"));
    }

    @Test
    void should_print_error_message_for_after_hooks() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    emptyList(),
                    singletonList(new StubStepDefinition("first step", "path/step_definitions.java:3")),
                    singletonList(new StubHookDefinition(new StubException()))))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "  Given first step      # path/step_definitions.java:3\n" +
                "      the stack trace\n"));
    }

    @Test
    void should_print_output_from_before_hooks() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition(testCaseState -> testCaseState.log("printed from hook"))),
                    singletonList(new StubStepDefinition("first step", "path/step_definitions.java:3")),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "\n" +
                "    printed from hook\n" +
                "\n" +
                "  Given first step      # path/step_definitions.java:3\n"));
    }

    @Test
    void should_print_output_from_after_hooks() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    emptyList(),
                    singletonList(new StubStepDefinition("first step", "path/step_definitions.java:3")),
                    singletonList(new StubHookDefinition(testCaseState -> testCaseState.log("printed from hook")))))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "  Given first step      # path/step_definitions.java:3\n" +
                "\n" +
                "    printed from hook\n"));
    }

    @Test
    void should_print_output_from_afterStep_hooks() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    emptyList(),
                    emptyList(),
                    asList(
                        new StubStepDefinition("first step", "path/step_definitions.java:3"),
                        new StubStepDefinition("second step", "path/step_definitions.java:4")),
                    singletonList(
                        new StubHookDefinition(testCaseState -> testCaseState.log("printed from afterstep hook"))),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
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
    void should_color_code_steps_according_to_the_result() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", "path/step_definitions.java:3")))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "  " + AnsiEscapes.GREEN + "Given " + AnsiEscapes.RESET + AnsiEscapes.GREEN + "first step"
                + AnsiEscapes.RESET));
    }

    @Test
    void should_color_code_locations_as_comments() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", "path/step_definitions.java:3")))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                AnsiEscapes.GREY + "# path/step_definitions.java:3" + AnsiEscapes.RESET + "\n"));
    }

    @Test
    void should_color_code_error_message_according_to_the_result() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", "path/step_definitions.java:3", new StubException())))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "      " + AnsiEscapes.RED + "the stack trace" + AnsiEscapes.RESET + "\n"));
    }

    @Test
    void should_mark_subsequent_arguments_in_steps() {
        Formats formats = ansi();

        StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry, bus);
        StepDefinition stepDefinition = new StubStepDefinition("text {string} text {string}", String.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);

        PrettyFormatter prettyFormatter = new PrettyFormatter(new ByteArrayOutputStream());
        String stepText = "text 'arg1' text 'arg2'";
        String formattedText = prettyFormatter.formatStepText("Given ", stepText, formats.get("passed"),
            formats.get("passed_arg"), createArguments(expression.match(stepText)));

        assertThat(formattedText, equalTo(AnsiEscapes.GREEN + "Given " + AnsiEscapes.RESET +
                AnsiEscapes.GREEN + "text " + AnsiEscapes.RESET +
                AnsiEscapes.GREEN + AnsiEscapes.INTENSITY_BOLD + "'arg1'" + AnsiEscapes.RESET +
                AnsiEscapes.GREEN + " text " + AnsiEscapes.RESET +
                AnsiEscapes.GREEN + AnsiEscapes.INTENSITY_BOLD + "'arg2'" + AnsiEscapes.RESET));
    }

    @Test
    void should_mark_nested_argument_as_part_of_full_argument() {
        Formats formats = ansi();

        StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry, bus);
        StepDefinition stepDefinition = new StubStepDefinition("^the order is placed( and (not yet )?confirmed)?$",
            String.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);

        PrettyFormatter prettyFormatter = new PrettyFormatter(new ByteArrayOutputStream());
        String stepText = "the order is placed and not yet confirmed";

        String formattedText = prettyFormatter.formatStepText("Given ", stepText, formats.get("passed"),
            formats.get("passed_arg"), createArguments(expression.match(stepText)));

        assertThat(formattedText, equalTo(AnsiEscapes.GREEN + "Given " + AnsiEscapes.RESET +
                AnsiEscapes.GREEN + "the order is placed" + AnsiEscapes.RESET +
                AnsiEscapes.GREEN + AnsiEscapes.INTENSITY_BOLD + " and not yet confirmed" + AnsiEscapes.RESET));
    }

    @Test
    void should_mark_nested_arguments_as_part_of_enclosing_argument() {
        Formats formats = ansi();
        PrettyFormatter prettyFormatter = new PrettyFormatter(new ByteArrayOutputStream());
        StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry, bus);
        StepDefinition stepDefinition = new StubStepDefinition("^the order is placed( and (not( yet)? )?confirmed)?$",
            String.class);
        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
        String stepText = "the order is placed and not yet confirmed";
        String formattedText = prettyFormatter.formatStepText("Given ", stepText, formats.get("passed"),
            formats.get("passed_arg"), createArguments(expression.match(stepText)));

        assertThat(formattedText, equalTo(AnsiEscapes.GREEN + "Given " + AnsiEscapes.RESET +
                AnsiEscapes.GREEN + "the order is placed" + AnsiEscapes.RESET +
                AnsiEscapes.GREEN + AnsiEscapes.INTENSITY_BOLD + " and not yet confirmed" + AnsiEscapes.RESET));
    }

}
