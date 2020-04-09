package io.cucumber.core.plugin;

import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runner.TestHelper;
import io.cucumber.plugin.event.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cucumber.core.runner.TestHelper.createAttachHookAction;
import static io.cucumber.core.runner.TestHelper.createWriteHookAction;
import static io.cucumber.core.runner.TestHelper.hookEntry;
import static io.cucumber.core.runner.TestHelper.result;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@DisabledOnOs(OS.WINDOWS)
class TeamCityPluginTest {

    private final List<Feature> features = new ArrayList<>();
    private final Map<String, Result> stepsToResult = new HashMap<>();
    private final Map<String, String> stepsToLocation = new HashMap<>();
    private final List<SimpleEntry<String, Result>> hooks = new ArrayList<>();
    private final List<String> hookLocations = new ArrayList<>();
    private final List<Answer<Object>> hookActions = new ArrayList<>();
    private final String location = new File("").toURI().toString();

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
        features.add(feature);
        stepsToLocation.put("first step", "com.example.StepDefinition.firstStep()");
        stepsToLocation.put("second step", "com.example.StepDefinition.secondStep()");
        stepsToLocation.put("third step", "com.example.StepDefinition.thirdStep()");

        String formatterOutput = runFeaturesWithFormatter();

        assertThat(formatterOutput, containsString("" +
            "##teamcity[enteredTheMatrix timestamp = '1970-01-01T12:00:00.000+0000']\n" +
            "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' name = 'Cucumber']\n" +
            "##teamcity[customProgressStatus testsCategory = 'Scenarios' count = '0' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
            "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location + "path/test.feature:1' name = 'feature name']\n" +
            "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location + "path/test.feature:2' name = '<name>']\n" +
            "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location + "path/test.feature:5' name = 'examples name']\n" +
            "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location + "path/test.feature:7' name = 'Example #1']\n" +
            "##teamcity[customProgressStatus type = 'testStarted' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
            "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location + "path/test.feature:3' captureStandardOutput = 'true' name = 'first step']\n" +
            "##teamcity[testFinished timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' name = 'first step']\n" +
            "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location + "path/test.feature:4' captureStandardOutput = 'true' name = 'second step']\n" +
            "##teamcity[testFinished timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' name = 'second step']\n" +
            "##teamcity[customProgressStatus type = 'testFinished' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
            "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = 'Example #1']\n" +
            "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location + "path/test.feature:8' name = 'Example #2']\n" +
            "##teamcity[customProgressStatus type = 'testStarted' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
            "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location + "path/test.feature:3' captureStandardOutput = 'true' name = 'first step']\n" +
            "##teamcity[testFinished timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' name = 'first step']\n" +
            "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = '" + location + "path/test.feature:4' captureStandardOutput = 'true' name = 'third step']\n" +
            "##teamcity[testFinished timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' name = 'third step']\n" +
            "##teamcity[customProgressStatus type = 'testFinished' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
            "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = 'Example #2']\n" +
            "##teamcity[customProgressStatus testsCategory = '' count = '0' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
            "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = 'examples name']\n" +
            "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = '<name>']\n" +
            "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = 'feature name']\n" +
            "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:00.000+0000' name = 'Cucumber']\n"
        ));
    }

    @Test
    void should_handle_nameless_attach_events() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "com.example.StepDefinition.firstStep()");
        stepsToResult.put("first step", result("passed"));
        stepsToLocation.put("first step", "com.example.StepDefinition.firstStep()");

        hooks.add(hookEntry("before", result("passed")));
        hookLocations.add("Hooks.before_hook_3()");
        hookActions.add(createAttachHookAction("A message".getBytes(), "text/plain"));

        String formatterOutput = runFeaturesWithFormatter();

        assertThat(formatterOutput, containsString("" +
            "##teamcity[message text='Embed event: |[text/plain 9 bytes|]|n' status='NORMAL']\n"
        ));
    }

    @Test
    void should_handle_write_events() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "com.example.StepDefinition.firstStep()");
        stepsToResult.put("first step", result("passed"));
        stepsToLocation.put("first step", "com.example.StepDefinition.firstStep()");

        hooks.add(hookEntry("before", result("passed")));
        hookLocations.add("Hooks.before_hook_1()");
        hookActions.add(createWriteHookAction("A message"));

        String formatterOutput = runFeaturesWithFormatter();

        assertThat(formatterOutput, containsString("" +
            "##teamcity[message text='Write event:|nA message|n' status='NORMAL']\n"
        ));
    }

    @Test
    void should_handle_attach_events() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "com.example.StepDefinition.firstStep()");
        stepsToResult.put("first step", result("passed"));
        stepsToLocation.put("first step", "com.example.StepDefinition.firstStep()");

        hooks.add(hookEntry("before", result("passed")));
        hookLocations.add("Hooks.before_hook_3()");
        hookActions.add(createAttachHookAction("A message".getBytes(), "text/plain", "message.txt"));

        String formatterOutput = runFeaturesWithFormatter();

        assertThat(formatterOutput, containsString("" +
            "##teamcity[message text='Embed event: message.txt |[text/plain 9 bytes|]|n' status='NORMAL']\n"
        ));
    }

    @Test
    void should_print_error_message_for_failed_steps() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "com.example.StepDefinition.firstStep()");
        stepsToResult.put("first step", result("failed"));

        String formatterOutput = runFeaturesWithFormatter();

        assertThat(formatterOutput, containsString("" +
            "##teamcity[testFailed timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' message = 'Step failed' details = 'the stack trace' name = 'first step']\n"
        ));
    }

    @Test
    void should_print_error_message_for_undefined_steps() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "com.example.StepDefinition.firstStep()");
        stepsToResult.put("first step", result("undefined"));

        String formatterOutput = runFeaturesWithFormatter();

        assertThat(formatterOutput, containsString("" +
            "##teamcity[testFailed timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' message = 'Step undefined' details = 'You can implement missing steps with the snippets below:|n|n' name = 'first step']\n"
        ));
    }

    @Test
    void should_print_error_message_for_before_hooks() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Given first step\n");
        features.add(feature);
        stepsToLocation.put("first step", "com.example.StepDefinition.firstStep()");
        stepsToResult.put("first step", result("passed"));
        hooks.add(hookEntry("before", result("failed")));
        hookLocations.add("com.example.HookDefinition.beforeHook()");

        String formatterOutput = runFeaturesWithFormatter();

        assertThat(formatterOutput, containsString("" +
            "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = 'java:test://com.example.HookDefinition/beforeHook' captureStandardOutput = 'true' name = 'Before']\n" +
            "##teamcity[testFailed timestamp = '1970-01-01T12:00:00.000+0000' duration = '0' message = 'Step failed' details = 'the stack trace' name = 'Before']"
        ));
    }

    private String runFeaturesWithFormatter() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        final TeamCityPlugin formatter = new TeamCityPlugin(printStream);

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

        return new String(byteArrayOutputStream.toByteArray(), UTF_8);
    }

}
