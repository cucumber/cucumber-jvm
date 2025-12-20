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
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.Duration;
import java.util.UUID;

import static io.cucumber.core.plugin.Bytes.bytes;
import static io.cucumber.core.plugin.IsEqualCompressingLineSeparators.equalCompressingLineSeparators;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.threeReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static org.hamcrest.MatcherAssert.assertThat;

@DisabledOnOs(OS.WINDOWS)
class TeamCityPluginTest {

    @Test
    void writes_teamcity_report() {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name
                  Scenario: scenario name
                    Given first step
                    When second step
                    Then third step
                """);

        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofSeconds(1));
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

        String featureFile = new File("").toPath().toUri() + "path/test.feature";
        assertThat(out,
            bytes(equalCompressingLineSeparators(
                """
                        ##teamcity[enteredTheMatrix timestamp = '1970-01-01T12:00:00.000+0000']
                        ##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' name = 'Cucumber']
                        ##teamcity[customProgressStatus testsCategory = 'Scenarios' count = '0' timestamp = '1970-01-01T12:00:00.000+0000']
                        ##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = 'path/test.feature:1' name = 'feature name']
                        ##teamcity[testSuiteStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = 'path/test.feature:2' name = 'scenario name']
                        ##teamcity[customProgressStatus type = 'testStarted' timestamp = '1970-01-01T12:00:00.000+0000']
                        ##teamcity[testStarted timestamp = '1970-01-01T12:00:00.000+0000' locationHint = 'path/test.feature:3' captureStandardOutput = 'true' name = 'first step']
                        ##teamcity[testFinished timestamp = '1970-01-01T12:00:01.000+0000' duration = '1000' name = 'first step']
                        ##teamcity[testStarted timestamp = '1970-01-01T12:00:01.000+0000' locationHint = 'path/test.feature:4' captureStandardOutput = 'true' name = 'second step']
                        ##teamcity[testFinished timestamp = '1970-01-01T12:00:02.000+0000' duration = '1000' name = 'second step']
                        ##teamcity[testStarted timestamp = '1970-01-01T12:00:02.000+0000' locationHint = 'path/test.feature:5' captureStandardOutput = 'true' name = 'third step']
                        ##teamcity[testFinished timestamp = '1970-01-01T12:00:03.000+0000' duration = '1000' name = 'third step']
                        ##teamcity[customProgressStatus type = 'testFinished' timestamp = '1970-01-01T12:00:03.000+0000']
                        ##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:03.000+0000' name = 'scenario name']
                        ##teamcity[customProgressStatus testsCategory = '' count = '0' timestamp = '1970-01-01T12:00:03.000+0000']
                        ##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:03.000+0000' name = 'feature name']
                        ##teamcity[testSuiteFinished timestamp = '1970-01-01T12:00:03.000+0000' name = 'Cucumber']"""
                        .replaceAll("path/test.feature", featureFile))));
    }

}
