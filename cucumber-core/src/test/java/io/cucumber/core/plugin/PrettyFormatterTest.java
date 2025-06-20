package io.cucumber.core.plugin;

import io.cucumber.core.backend.SourceReference;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.StubHookDefinition;
import io.cucumber.core.backend.StubStaticHookDefinition;
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
import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.util.Locale;
import java.util.UUID;

import static io.cucumber.core.plugin.AnsiEscapes.GREEN;
import static io.cucumber.core.plugin.AnsiEscapes.GREY;
import static io.cucumber.core.plugin.AnsiEscapes.INTENSITY_BOLD;
import static io.cucumber.core.plugin.AnsiEscapes.RED;
import static io.cucumber.core.plugin.AnsiEscapes.RESET;
import static io.cucumber.core.plugin.AnsiEscapes.YELLOW;
import static io.cucumber.core.plugin.Bytes.bytes;
import static io.cucumber.core.plugin.Formats.ansi;
import static io.cucumber.core.plugin.IsEqualCompressingLineSeparators.equalCompressingLineSeparators;
import static io.cucumber.core.runner.TestDefinitionArgument.createArguments;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                    new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference()),
                    new StubStepDefinition("second step", PrettyFormatterStepDefinition.twoReference()),
                    new StubStepDefinition("third step", PrettyFormatterStepDefinition.threeReference())))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "\n" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "  When second step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\n" +
                "  Then third step       # io.cucumber.core.plugin.PrettyFormatterStepDefinition.three()\n")));
    }

    @Test
    void should_skip_missing_location_strings() {
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
                        new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference()),
                        new StubStepDefinition("second step", (SourceReference) null),
                        new StubStepDefinition("third step", PrettyFormatterStepDefinition.threeReference())))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "\n" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "  When second step\n" +
                "  Then third step       # io.cucumber.core.plugin.PrettyFormatterStepDefinition.three()\n")));
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
                        new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference()),
                        new StubStepDefinition("second step", PrettyFormatterStepDefinition.twoReference()),
                        new StubStepDefinition("third step", PrettyFormatterStepDefinition.threeReference())))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "\n" +
                "Scenario: s1       # path/test.feature:4\n" +
                "  Given first step # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "  Then second step # io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\n" +
                "\n" +
                "Scenario: s2       # path/test.feature:6\n" +
                "  Given first step # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "  Then third step  # io.cucumber.core.plugin.PrettyFormatterStepDefinition.three()\n")));
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
                        new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference()),
                        new StubStepDefinition("second step", PrettyFormatterStepDefinition.twoReference()),
                        new StubStepDefinition("third step", PrettyFormatterStepDefinition.threeReference())))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "\n" +
                "Scenario Outline: name 1 # path/test.feature:7\n" +
                "  Given first step       # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "  Then second step       # io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\n" +
                "\n" +
                "Scenario Outline: name 2 # path/test.feature:8\n" +
                "  Given first step       # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "  Then third step        # io.cucumber.core.plugin.PrettyFormatterStepDefinition.three()\n")));
    }

    @Test
    void should_print_encoded_characters() {

        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test Characters\n" +
                "    Given first step\n" +
                "      | URLEncoded | %71s%22i%22%3A%7B%22D |\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", PrettyFormatterStepDefinition.twoReference(), DataTable.class)))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +

                "\n" +
                "Scenario: Test Characters # path/test.feature:2\n" +
                "  Given first step        # io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\n" +
                "    | URLEncoded | %71s%22i%22%3A%7B%22D |\n")));
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
                        new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference()),
                        new StubStepDefinition("second step", PrettyFormatterStepDefinition.twoReference())))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +

                "\n" +
                "@feature_tag @scenario_tag\n" +
                "Scenario: scenario name # path/test.feature:4\n" +
                "  Then first step       # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "\n" +
                "@feature_tag @scenario_outline_tag @examples_tag\n" +
                "Scenario Outline: scenario outline name # path/test.feature:12\n" +
                "  Then second step                      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\n")));
    }

    @Test
    void should_print_table() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test Scenario\n" +
                "    Given first step\n" +
                "      | key1     | key2     |\n" +
                "      | value1   | value2   |\n" +
                "      | another1 | another2 |\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference(), DataTable.class)))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +

                "\n" +
                "Scenario: Test Scenario # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "    | key1     | key2     |\n" +
                "    | value1   | value2   |\n" +
                "    | another1 | another2 |\n")));
    }

    @Test
    void should_print_multiple_tables() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test Scenario\n" +
                "    Given first step\n" +
                "      | key1     | key2     |\n" +
                "      | value1   | value2   |\n" +
                "      | another1 | another2 |\n" +
                "    Given second step\n" +
                "      | key3     | key4     |\n" +
                "      | value3   | value4   |\n" +
                "      | another3 | another4 |\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", "# io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()", DataTable.class),
                    new StubStepDefinition("second step", "path/step_definitions.java:15", DataTable.class)))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +

                "\n" +
                "Scenario: Test Scenario # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\n" +
                "    | key1     | key2     |\n" +
                "    | value1   | value2   |\n" +
                "    | another1 | another2 |\n" +
                "  Given second step     # path/step_definitions.java:15\n" +
                "    | key3     | key4     |\n" +
                "    | value3   | value4   |\n" +
                "    | another3 | another4 |\n")));
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
                    new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference(),
                        new StubException("the exception message")
                                .withClassName()
                                .withStacktrace("the stack trace"))))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "      io.cucumber.core.plugin.StubException\n" +
                "      the exception message\n" +
                "      \tthe stack trace\n")));
    }

    @Test
    void should_indent_stacktrace() {
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
                    new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference(),
                        new StubException("the exception message")
                                .withClassName()
                                .withStacktrace("the stack trace"))))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "      io.cucumber.core.plugin.StubException\n" +
                "      the exception message\n" +
                "      \tthe stack trace\n")));
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
                    singletonList(new StubHookDefinition(new StubException("the exception message")
                            .withClassName()
                            .withStacktrace("the stack trace"))),
                    singletonList(new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference())),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "      io.cucumber.core.plugin.StubException\n" +
                "      the exception message\n" +
                "      \tthe stack trace\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()")));
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
                    singletonList(new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference())),
                    singletonList(new StubHookDefinition(new StubException("the exception message")
                            .withClassName()
                            .withStacktrace("the stack trace")))))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "      io.cucumber.core.plugin.StubException\n" +
                "      the exception message\n" +
                "      \tthe stack trace\n")));
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
                    singletonList(new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference())),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "\n" +
                "    printed from hook\n" +
                "\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n")));
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
                    singletonList(new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference())),
                    singletonList(new StubHookDefinition(testCaseState -> testCaseState.log("printed from hook")))))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "\n" +
                "    printed from hook\n")));
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
                        new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference()),
                        new StubStepDefinition("second step", PrettyFormatterStepDefinition.twoReference())),
                    singletonList(
                        new StubHookDefinition(testCaseState -> testCaseState.log("printed from afterstep hook"))),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "\n" +
                "    printed from afterstep hook\n" +
                "\n" +
                "  When second step      # path/step_definitions.java:4\n" +
                "\n" +
                "    printed from afterstep hook" +
                "\n")));
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
                    new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference())))
                .build()
                .run();

        assertThat(out, bytes(containsString("" +
                "  " + GREEN + "Given " + RESET + GREEN + "first step"
                + RESET)));
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
                    new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference())))
                .build()
                .run();

        assertThat(out, bytes(containsString("" +
                GREY + "# io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()" + RESET)));
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
                    new StubStepDefinition("first step", PrettyFormatterStepDefinition.oneReference(),
                        new StubException("the exception message")
                                .withClassName()
                                .withStacktrace("the stack trace"))))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name " + GREY + "# path/test.feature:2" + RESET + "\n" +
                "  " + RED + "Given " + RESET + RED + "first step" + RESET + "      " + GREY
                + "# io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()" + RESET + "\n" +
                "      " + RED + "io.cucumber.core.plugin.StubException\n" +
                "      the exception message\n" +
                "      \tthe stack trace" + RESET + "\n")));
    }

//    @Test
//    void should_mark_subsequent_arguments_in_steps() {
//        Formats formats = ansi();
//
//        StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
//        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry, bus);
//        StepDefinition stepDefinition = new StubStepDefinition("text {string} text {string}", String.class);
//        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
//
//        PrettyFormatter prettyFormatter = new PrettyFormatter(new ByteArrayOutputStream());
//        String stepText = "text 'arg1' text 'arg2'";
//        String formattedText = prettyFormatter.formatStepText("Given ", stepText, formats.get("passed"),
//            formats.get("passed_arg"), expression.match(stepText));
//
//        assertThat(formattedText, equalTo(GREEN + "Given " + RESET +
//                GREEN + "text " + RESET +
//                GREEN + INTENSITY_BOLD + "'arg1'" + RESET +
//                GREEN + " text " + RESET +
//                GREEN + INTENSITY_BOLD + "'arg2'" + RESET));
//    }
//
//    @Test
//    void should_mark_nested_argument_as_part_of_full_argument() {
//        Formats formats = ansi();
//
//        StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
//        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry, bus);
//        StepDefinition stepDefinition = new StubStepDefinition("^the order is placed( and (not yet )?confirmed)?$",
//            String.class);
//        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
//
//        PrettyFormatter prettyFormatter = new PrettyFormatter(new ByteArrayOutputStream());
//        String stepText = "the order is placed and not yet confirmed";
//
//        String formattedText = prettyFormatter.formatStepText("Given ", stepText, formats.get("passed"),
//            formats.get("passed_arg"), createArguments(expression.match(stepText)));
//
//        assertThat(formattedText, equalTo(GREEN + "Given " + RESET +
//                GREEN + "the order is placed" + RESET +
//                GREEN + INTENSITY_BOLD + " and not yet confirmed" + RESET));
//    }
//
//    @Test
//    void should_mark_nested_arguments_as_part_of_enclosing_argument() {
//        Formats formats = ansi();
//        PrettyFormatter prettyFormatter = new PrettyFormatter(new ByteArrayOutputStream());
//        StepTypeRegistry registry = new StepTypeRegistry(Locale.ENGLISH);
//        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(registry, bus);
//        StepDefinition stepDefinition = new StubStepDefinition("^the order is placed( and (not( yet)? )?confirmed)?$",
//            String.class);
//        StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
//        String stepText = "the order is placed and not yet confirmed";
//        String formattedText = prettyFormatter.formatStepText("Given ", stepText, formats.get("passed"),
//            formats.get("passed_arg"), createArguments(expression.match(stepText)));
//
//        assertThat(formattedText, equalTo(GREEN + "Given " + RESET +
//                GREEN + "the order is placed" + RESET +
//                GREEN + INTENSITY_BOLD + " and not yet confirmed" + RESET));
//    }

    @Test
    void should_print_system_failure_for_failed_hooks() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(StubException.class, () -> Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    singletonList(new StubStaticHookDefinition(new StubException("Hook failed")
                            .withClassName()
                            .withStacktrace("the stack trace")))))
                .build()
                .run());

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name " + GREY + "# path/test.feature:2" + RESET + "\n" +
                "  " + YELLOW + "Given " + RESET + YELLOW + "first step" + RESET + "\n" +
                RED + "io.cucumber.core.plugin.StubException\n" +
                "Hook failed\n" +
                "\tthe stack trace" + RESET + "\n")));
    }

    @Test
    void should_print_docstring_including_content_type() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: Test Scenario\n" +
                "    Given first step\n" +
                "    \"\"\"json\n" +
                "    {\"key1\": \"value1\",\n" +
                "     \"key2\": \"value2\",\n" +
                "     \"another1\": \"another2\"}\n" +
                "    \"\"\"\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", "# io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()", DocString.class)))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "\n" +
                "Scenario: Test Scenario # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\n" +
                "    \"\"\"json\n" +
                "    {\"key1\": \"value1\",\n" +
                "     \"key2\": \"value2\",\n" +
                "     \"another1\": \"another2\"}\n" +
                "    \"\"\"\n")));
    }
}
