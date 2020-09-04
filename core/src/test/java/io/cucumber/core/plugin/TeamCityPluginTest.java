package io.cucumber.core.plugin;

import io.cucumber.core.backend.StubHookDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.UUID;

import static io.cucumber.core.plugin.BytesContainsString.bytesContainsString;
import static java.time.Clock.fixed;
import static java.time.Instant.EPOCH;
import static java.time.ZoneId.of;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

@DisabledOnOs(OS.WINDOWS)
class TeamCityPluginTest {

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
                .withAdditionalPlugins(new TeamCityPlugin(new PrintStream(out)))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step"),
                    new StubStepDefinition("second step"),
                    new StubStepDefinition("third step")))
                .build()
                .run();

        String location = new File("").toURI().toString();

        String expected = "" +
                "##teamcity[enteredTheMatrix timestamp = '1970-01-01T12:00:00.000+0000']\n" +
                "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' name = 'Cucumber']\n" +
                "##teamcity[customProgressStatus testsCategory = 'Scenarios' count = '0' timestamp = '1970-01-01T12:00:00.000+0000']\n"
                +
                "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location
                + "path/test.feature:1' name = 'feature name']\n" +
                "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location
                + "path/test.feature:2' name = '<name>']\n" +
                "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location
                + "path/test.feature:5' name = 'examples name']\n" +
                "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location
                + "path/test.feature:7' name = 'Example #1']\n" +
                "##teamcity[customProgressStatus type = 'testStarted' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
                "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location
                + "path/test.feature:3' captureStandardOutput = 'true' name = 'first step']\n" +
                "##teamcity[testFinished timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' name = 'first step']\n"
                +
                "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location
                + "path/test.feature:4' captureStandardOutput = 'true' name = 'second step']\n" +
                "##teamcity[testFinished timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' name = 'second step']\n"
                +
                "##teamcity[customProgressStatus type = 'testFinished' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
                "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = 'Example #1']\n" +
                "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location
                + "path/test.feature:8' name = 'Example #2']\n" +
                "##teamcity[customProgressStatus type = 'testStarted' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
                "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location
                + "path/test.feature:3' captureStandardOutput = 'true' name = 'first step']\n" +
                "##teamcity[testFinished timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' name = 'first step']\n"
                +
                "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location
                + "path/test.feature:4' captureStandardOutput = 'true' name = 'third step']\n" +
                "##teamcity[testFinished timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' name = 'third step']\n"
                +
                "##teamcity[customProgressStatus type = 'testFinished' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
                "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = 'Example #2']\n" +
                "##teamcity[customProgressStatus testsCategory = '' count = '0' timestamp = '1970-01-01T12:00:00.000+0000']\n"
                +
                "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = 'examples name']\n" +
                "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = '<name>']\n" +
                "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = 'feature name']\n" +
                "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = 'Cucumber']\n";

        assertThat(out, bytesContainsString(expected));
    }

    @Test
    void should_handle_nameless_attach_events() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TeamCityPlugin(new PrintStream(out)))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition(
                        (TestCaseState state) -> state.attach("A message", "text/plain", null))),
                    singletonList(new StubStepDefinition("first step")),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "##teamcity[message text='Embed event: |[text/plain 9 bytes|]|n' status='NORMAL']\n"));
    }

    @Test
    void should_handle_write_events() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TeamCityPlugin(new PrintStream(out)))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition((TestCaseState state) -> state.log("A message"))),
                    singletonList(new StubStepDefinition("first step")),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "##teamcity[message text='Write event:|nA message|n' status='NORMAL']\n"));
    }

    @Test
    void should_handle_attach_events() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TeamCityPlugin(new PrintStream(out)))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition(
                        (TestCaseState state) -> state.attach("A message", "text/plain", "message.txt"))),
                    singletonList(new StubStepDefinition("first step")),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "##teamcity[message text='Embed event: message.txt |[text/plain 9 bytes|]|n' status='NORMAL']\n"));
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
                .withAdditionalPlugins(new TeamCityPlugin(new PrintStream(out)))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", new StubException("Step failed", "the stack trace"))))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "##teamcity[testFailed timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' message = 'Step failed' details = 'the stack trace' name = 'first step']\n"));
    }

    @Test
    void should_print_error_message_for_undefined_steps() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TeamCityPlugin(new PrintStream(out)))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier())
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "##teamcity[testFailed timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' message = 'Step undefined' details = 'You can implement missing steps with the snippets below:|n|n' name = 'first step']\n"));
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
                .withAdditionalPlugins(new TeamCityPlugin(new PrintStream(out)))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition(new StubException("Step failed", "the stack trace"))),
                    singletonList(new StubStepDefinition("first step")),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '{stubbed location with details}' captureStandardOutput = 'true' name = 'Before']\n"
                +
                "##teamcity[testFailed timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' message = 'Step failed' details = 'the stack trace' name = 'Before']"));
    }

    @Test
    void should_print_location_hint_for_hooks() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new TeamCityPlugin(new PrintStream(out)))
                .withEventBus(new TimeServiceEventBus(fixed(EPOCH, of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition("com.example.HookDefinition.beforeHook()")),
                    singletonList(new StubStepDefinition("first step")),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytesContainsString("" +
                "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = 'java:test://com.example.HookDefinition/beforeHook' captureStandardOutput = 'true' name = 'Before']\n"));
    }

}
