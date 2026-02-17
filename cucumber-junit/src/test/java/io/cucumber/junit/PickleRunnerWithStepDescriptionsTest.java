package io.cucumber.junit;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.Options;
import io.cucumber.core.runtime.CucumberExecutionContext;
import io.cucumber.core.runtime.ExitStatus;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.junit.PickleRunners.PickleRunner;
import io.cucumber.junit.PickleRunners.WithStepDescriptions;
import io.cucumber.plugin.event.Step;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import static io.cucumber.junit.TestPickleBuilder.picklesFromFeature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

final class PickleRunnerWithStepDescriptionsTest {

    final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    final Options options = RuntimeOptions.defaultOptions();
    final RunnerSupplier runnerSupplier = mock(RunnerSupplier.class);
    final CucumberExecutionContext context = new CucumberExecutionContext(bus, new ExitStatus(options), runnerSupplier);

    @Test
    void shouldAssignUnequalDescriptionsToDifferentOccurrencesOfSameStepInAScenario() {
        List<Pickle> pickles = picklesFromFeature("path/test.feature", """
                Feature: FB
                # Scenario with same step occurring twice

                  Scenario: SB
                    When foo
                    Then bar

                    When foo
                    Then baz
                """);

        WithStepDescriptions runner = (WithStepDescriptions) PickleRunners.withStepDescriptions(
            context,
            pickles.get(0),
            null,
            createJunitOptions());

        // fish out the two occurrences of the same step and check whether we
        // really got them
        Step stepOccurrence1 = runner.getChildren().get(0);
        Step stepOccurrence2 = runner.getChildren().get(2);
        assertEquals(stepOccurrence1.getText(), stepOccurrence2.getText());

        // then check that the descriptions are unequal
        Description runnerDescription = runner.getDescription();

        Description stepDescription1 = runnerDescription.getChildren().get(0);
        Description stepDescription2 = runnerDescription.getChildren().get(2);

        assertNotEquals(stepDescription1, stepDescription2);
    }

    private JUnitOptions createJunitOptions() {
        return new JUnitOptionsBuilder().build();
    }

    @Test
    void shouldAssignUnequalDescriptionsToDifferentStepsInAScenarioOutline() {
        Feature features = TestPickleBuilder.parseFeature("path/test.feature", """
                Feature: FB
                  Scenario Outline: SO
                    When <action>
                    Then <result>
                    Examples:
                    | action | result |
                    |   a1   |   r1   |
                """);

        WithStepDescriptions runner = (WithStepDescriptions) PickleRunners.withStepDescriptions(
            context,
            features.getPickles().get(0),
            null,
            createJunitOptions());

        Description runnerDescription = runner.getDescription();
        Description stepDescription1 = runnerDescription.getChildren().get(0);
        Description stepDescription2 = runnerDescription.getChildren().get(1);

        assertNotEquals(stepDescription1, stepDescription2);
    }

    @Test
    void shouldIncludeScenarioNameAsClassNameInStepDescriptions() {
        Feature features = TestPickleBuilder.parseFeature("path/test.feature", """
                Feature: In cucumber.junit
                  Scenario: first
                    When step
                    Then another step

                  Scenario: second
                    When step
                    Then another step
                """);

        PickleRunner runner = PickleRunners.withStepDescriptions(
            context,
            features.getPickles().get(0),
            null,
            createJunitOptions());

        // fish out the data from runner
        Description runnerDescription = runner.getDescription();
        Description stepDescription = runnerDescription.getChildren().get(0);

        assertEquals("first", stepDescription.getClassName());
        assertEquals("step", stepDescription.getMethodName());
        assertEquals("step(first)", stepDescription.getDisplayName());

    }

    @Test
    void shouldUseScenarioNameForDisplayName() {
        List<Pickle> pickles = picklesFromFeature("featurePath", """
                Feature: feature name
                  Scenario: scenario name
                    Then it works
                """);

        PickleRunner runner = PickleRunners.withStepDescriptions(
            context,
            pickles.get(0),
            null,
            createJunitOptions());

        assertEquals("scenario name", runner.getDescription().getDisplayName());
    }

    @Test
    void shouldUseStepKeyworkAndNameForChildName() {
        List<Pickle> pickles = picklesFromFeature("featurePath", """
                Feature: feature name
                  Scenario: scenario name
                    Then it works
                """);

        PickleRunner runner = PickleRunners.withStepDescriptions(
            context,
            pickles.get(0),
            null,
            createJunitOptions());

        assertEquals("it works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    @Test
    void shouldConvertTextFromFeatureFileForNamesWithFilenameCompatibleNameOption() {
        List<Pickle> pickles = picklesFromFeature("featurePath", """
                Feature: feature name
                  Scenario: scenario name
                    Then it works
                """);

        PickleRunner runner = PickleRunners.withStepDescriptions(
            context,
            pickles.get(0),
            null,
            createFileNameCompatibleJunitOptions());

        assertEquals("scenario_name", runner.getDescription().getDisplayName());
        assertEquals("scenario_name", runner.getDescription().getChildren().get(0).getClassName());
        assertEquals("it_works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    private JUnitOptions createFileNameCompatibleJunitOptions() {
        return new JUnitOptionsBuilder().setFilenameCompatibleNames(true).build();
    }

}
