package io.cucumber.junit;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.junit.PickleRunners.PickleRunner;
import io.cucumber.junit.PickleRunners.WithStepDescriptions;
import io.cucumber.plugin.event.Step;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;

import java.util.List;

import static io.cucumber.junit.TestPickleBuilder.picklesFromFeature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

class PickleRunnerWithStepDescriptionsTest {

    @Test
    void shouldAssignUnequalDescriptionsToDifferentOccurrencesOfSameStepInAScenario() {
        List<Pickle> pickles = picklesFromFeature("path/test.feature", "" +
                "Feature: FB\n" +
                "# Scenario with same step occurring twice\n" +
                "\n" +
                "  Scenario: SB\n" +
                "    When foo\n" +
                "    Then bar\n" +
                "\n" +
                "    When foo\n" +
                "    Then baz\n");

        WithStepDescriptions runner = (WithStepDescriptions) PickleRunners.withStepDescriptions(
            mock(RunnerSupplier.class),
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
        Feature features = TestPickleBuilder.parseFeature("path/test.feature", "" +
                "Feature: FB\n" +
                "  Scenario Outline: SO\n" +
                "    When <action>\n" +
                "    Then <result>\n" +
                "    Examples:\n" +
                "    | action | result |\n" +
                "    |   a1   |   r1   |\n");

        WithStepDescriptions runner = (WithStepDescriptions) PickleRunners.withStepDescriptions(
            mock(RunnerSupplier.class),
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
        Feature features = TestPickleBuilder.parseFeature("path/test.feature", "" +
                "Feature: In cucumber.junit\n" +
                "  Scenario: first\n" +
                "    When step\n" +
                "    Then another step\n" +
                "\n" +
                "  Scenario: second\n" +
                "    When step\n" +
                "    Then another step\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
            mock(RunnerSupplier.class),
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
        List<Pickle> pickles = picklesFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
            mock(RunnerSupplier.class),
            pickles.get(0),
            null,
            createJunitOptions());

        assertEquals("scenario name", runner.getDescription().getDisplayName());
    }

    @Test
    void shouldUseStepKeyworkAndNameForChildName() {
        List<Pickle> pickles = picklesFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
            mock(RunnerSupplier.class),
            pickles.get(0),
            null,
            createJunitOptions());

        assertEquals("it works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    @Test
    void shouldConvertTextFromFeatureFileForNamesWithFilenameCompatibleNameOption() {
        List<Pickle> pickles = picklesFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
            mock(RunnerSupplier.class),
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
