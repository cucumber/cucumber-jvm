package io.cucumber.core.plugin;

import io.cucumber.core.backend.SourceReference;
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
import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.util.UUID;

import static io.cucumber.core.plugin.AnsiEscapes.GREEN;
import static io.cucumber.core.plugin.AnsiEscapes.GREY;
import static io.cucumber.core.plugin.AnsiEscapes.INTENSITY_BOLD;
import static io.cucumber.core.plugin.AnsiEscapes.RED;
import static io.cucumber.core.plugin.AnsiEscapes.RESET;
import static io.cucumber.core.plugin.AnsiEscapes.YELLOW;
import static io.cucumber.core.plugin.Bytes.bytes;
import static io.cucumber.core.plugin.IsEqualCompressingLineSeparators.equalCompressingLineSeparators;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneArgumentsReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoArgumentsReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
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
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference()),
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
                    new StubStepDefinition("first step", oneReference()),
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
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference()),
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
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference()),
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
                    new StubStepDefinition("first step", twoReference(), DataTable.class)))
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
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference())))
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
                    new StubStepDefinition("first step", oneReference(), DataTable.class)))
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
                    new StubStepDefinition("first step", oneReference(), DataTable.class),
                    new StubStepDefinition("second step", twoReference(), DataTable.class)))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +

                "\n" +
                "Scenario: Test Scenario # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "    | key1     | key2     |\n" +
                "    | value1   | value2   |\n" +
                "    | another1 | another2 |\n" +
                "  Given second step     # io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\n" +
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
                    new StubStepDefinition("first step", oneReference(),
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
                    new StubStepDefinition("first step", oneReference(),
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
                    singletonList(new StubStepDefinition("first step", oneReference())),
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
                    singletonList(new StubStepDefinition("first step", oneReference())),
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
                    singletonList(new StubStepDefinition("first step", oneReference())),
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
                    singletonList(new StubStepDefinition("first step", oneReference())),
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
                        new StubStepDefinition("first step", oneReference()),
                        new StubStepDefinition("second step", twoReference())),
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
                "  When second step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\n" +
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
                    new StubStepDefinition("first step", oneReference())))
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
                    new StubStepDefinition("first step", oneReference())))
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
                    new StubStepDefinition("first step", oneReference(),
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

    @Test
    void should_mark_subsequent_arguments_in_steps() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given around 31 cucumbers and 41 zucchinis\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("around {int} cucumbers and {int} zucchinis", twoArgumentsReference(),
                        Integer.class, Integer.class)))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators(
            "" +
                    "Scenario: scenario name                      " + GREY + "# path/test.feature:2" + RESET + "\n" +
                    "  " + GREEN + "Given " + RESET +
                    GREEN + "around " + RESET +
                    GREEN + INTENSITY_BOLD + "31" + RESET +
                    GREEN + " cucumbers and " + RESET +
                    GREEN + INTENSITY_BOLD + "41" + RESET +
                    GREEN + " zucchinis" + RESET +
                    " " +
                    GREY
                    + "# io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoArguments(java.lang.Integer,java.lang.Integer)"
                    + RESET + "\n")));
    }

    @Test
    void should_mark_nested_argument_as_part_of_full_argument() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given the order is placed and not yet confirmed\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("^the order is placed( and (not yet )?confirmed)?$", oneArgumentsReference(),
                        String.class)))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name                           " + GREY + "# path/test.feature:2" + RESET + "\n" +
                "  " + GREEN + "Given " + RESET +
                GREEN + "the order is placed" + RESET +
                GREEN + INTENSITY_BOLD + " and not yet confirmed" + RESET +
                " " +
                GREY + "# io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneArgument(java.lang.String)" + RESET
                + "\n")));
    }

    @Test
    void should_mark_nested_arguments_as_part_of_enclosing_argument() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given the order is placed and not yet confirmed\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("^the order is placed( and (not( yet)? )?confirmed)?$",
                        oneArgumentsReference(), String.class)))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "Scenario: scenario name                           " + GREY + "# path/test.feature:2" + RESET + "\n" +
                "  " + GREEN + "Given " + RESET +
                GREEN + "the order is placed" + RESET +
                GREEN + INTENSITY_BOLD + " and not yet confirmed" + RESET +
                " " +
                GREY + "# io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneArgument(java.lang.String)" + RESET
                + "\n")));
    }

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
                    new StubStepDefinition("first step", oneReference(), DocString.class)))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "\n" +
                "Scenario: Test Scenario # path/test.feature:2\n" +
                "  Given first step      # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n" +
                "    \"\"\"json\n" +
                "    {\"key1\": \"value1\",\n" +
                "     \"key2\": \"value2\",\n" +
                "     \"another1\": \"another2\"}\n" +
                "    \"\"\"\n")));
    }
}
