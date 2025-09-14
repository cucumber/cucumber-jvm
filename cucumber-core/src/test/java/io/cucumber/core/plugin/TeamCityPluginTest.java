package io.cucumber.core.plugin;

import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.UUID;

import static io.cucumber.core.plugin.Bytes.bytes;
import static io.cucumber.core.plugin.IsEqualCompressingLineSeparators.equalCompressingLineSeparators;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.threeReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static org.hamcrest.MatcherAssert.assertThat;

class TeamCityPluginTest {

    @Test
    void writes_teamcity_report() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");

        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofMillis(1000));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new TeamCityPlugin(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                        new StubStepDefinition("first step", oneReference()),
                        new StubStepDefinition("second step", twoReference()),
                        new StubStepDefinition("third step", threeReference())))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "##teamcity[enteredTheMatrix timestamp = '1970-01-01T12:00:00.000+0000']\n" +
                "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' name = 'Cucumber']\n" +
                "##teamcity[customProgressStatus testsCategory = 'Scenarios' count = '0' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
                "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = 'file:/home/mpkorstanje/Projects/cucumber/cucumber-jvm/cucumber-core/path/test.feature:1' name = 'feature name']\n" +
                "##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = 'file:/home/mpkorstanje/Projects/cucumber/cucumber-jvm/cucumber-core/path/test.feature:2' name = 'scenario name']\n" +
                "##teamcity[customProgressStatus type = 'testStarted' timestamp = '1970-01-01T12:00:00.000+0000']\n" +
                "##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = 'file:/home/mpkorstanje/Projects/cucumber/cucumber-jvm/cucumber-core/path/test.feature:3' captureStandardOutput = 'true' name = 'first step']\n" +
                "##teamcity[testFinished timestamp = '1970-01-01T12:00:01.000+0000' duration = '1000' name = 'first step']\n" +
                "##teamcity[testStarted timestamp = '1970-01-01T12:00:01.000+0000' locationHint = 'file:/home/mpkorstanje/Projects/cucumber/cucumber-jvm/cucumber-core/path/test.feature:4' captureStandardOutput = 'true' name = 'second step']\n" +
                "##teamcity[testFinished timestamp = '1970-01-01T12:00:02.000+0000' duration = '1000' name = 'second step']\n" +
                "##teamcity[testStarted timestamp = '1970-01-01T12:00:02.000+0000' locationHint = 'file:/home/mpkorstanje/Projects/cucumber/cucumber-jvm/cucumber-core/path/test.feature:5' captureStandardOutput = 'true' name = 'third step']\n" +
                "##teamcity[testFinished timestamp = '1970-01-01T12:00:03.000+0000' duration = '1000' name = 'third step']\n" +
                "##teamcity[customProgressStatus type = 'testFinished' timestamp = '1970-01-01T12:00:03.000+0000']\n" +
                "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:03.000+0000' name = 'scenario name']\n" +
                "##teamcity[customProgressStatus testsCategory = '' count = '0' timestamp = '1970-01-01T12:00:03.000+0000']\n" +
                "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:03.000+0000' name = 'feature name']\n" +
                "##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:03.000+0000' name = 'Cucumber']")));
    }


}
