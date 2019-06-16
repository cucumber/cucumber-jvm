package io.cucumber.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import cucumber.runner.RunnerSupplier;
import cucumber.runtime.model.CucumberFeature;
import io.cucumber.junit.PickleRunners.PickleRunner;
import io.cucumber.junit.PickleRunners.WithStepDescriptions;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleStep;
import org.junit.Test;
import org.junit.runner.Description;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PickleRunnerWithStepDescriptionsTest {

    @Test
    public void shouldAssignUnequalDescriptionsToDifferentOccurrencesOfSameStepInAScenario() throws Exception {
        CucumberFeature features = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: FB\n" +
            "# Scenario with same step occurring twice\n" +
            "\n" +
            "  Scenario: SB\n" +
            "    When foo\n" +
            "    Then bar\n" +
            "\n" +
            "    When foo\n" +
            "    Then baz\n"
        );

        Compiler compiler = new Compiler();
        List<PickleEvent> pickleEvents = new ArrayList<>();
        for (Pickle pickle : compiler.compile(features.getGherkinFeature())) {
            pickleEvents.add(new PickleEvent(features.getUri().toString(), pickle));
        };

        WithStepDescriptions runner = (WithStepDescriptions) PickleRunners.withStepDescriptions(
                mock(RunnerSupplier.class),
                pickleEvents.get(0),
                createJUnitOptions()
        );

        // fish out the two occurrences of the same step and check whether we really got them
        PickleStep stepOccurrence1 = runner.getChildren().get(0);
        PickleStep stepOccurrence2 = runner.getChildren().get(2);
        assertEquals(stepOccurrence1.getText(), stepOccurrence2.getText());

        // then check that the descriptions are unequal
        Description runnerDescription = runner.getDescription();

        Description stepDescription1 = runnerDescription.getChildren().get(0);
        Description stepDescription2 = runnerDescription.getChildren().get(2);

        assertFalse("Descriptions must not be equal.", stepDescription1.equals(stepDescription2));
    }

    @Test
    public void shouldAssignUnequalDescriptionsToDifferentStepsInAScenarioOutline() throws Exception {
        CucumberFeature features = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: FB\n" +
            "  Scenario Outline: SO\n" +
            "    When <action>\n" +
            "    Then <result>\n" +
            "    Examples:\n" +
            "    | action | result |\n" +
            "    |   a1   |   r1   |\n"
        );

        WithStepDescriptions runner = (WithStepDescriptions) PickleRunners.withStepDescriptions(
                mock(RunnerSupplier.class),
                features.getPickles().get(0),
                createJUnitOptions()
        );

        Description runnerDescription = runner.getDescription();
        Description stepDescription1 = runnerDescription.getChildren().get(0);
        Description stepDescription2 = runnerDescription.getChildren().get(1);

        assertFalse("Descriptions must not be equal.", stepDescription1.equals(stepDescription2));
    }

    @Test
    public void shouldIncludeScenarioNameAsClassNameInStepDescriptions() throws Exception {
        CucumberFeature features = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: In cucumber.junit\n" +
            "  Scenario: first\n" +
            "    When step\n" +
            "    Then another step\n" +
            "\n" +
            "  Scenario: second\n" +
            "    When step\n" +
            "    Then another step\n"
        );

        PickleRunner runner = PickleRunners.withStepDescriptions(
                mock(RunnerSupplier.class),
                features.getPickles().get(0),
                createJUnitOptions()
        );

        // fish out the data from runner
        Description runnerDescription = runner.getDescription();
        Description stepDescription = runnerDescription.getChildren().get(0);

        assertEquals("description includes scenario name as class name", "first", stepDescription.getClassName());
        assertEquals("description includes step keyword and name as method name", "step", stepDescription.getMethodName());
        assertEquals("description includes scenario and step name in display name", "step(first)", stepDescription.getDisplayName());

    }

    @Test
    public void shouldUseScenarioNameForDisplayName() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
                mock(RunnerSupplier.class),
                pickles.get(0),
                createJUnitOptions()
        );

        assertEquals("scenario name", runner.getDescription().getDisplayName());
    }

    @Test
    public void shouldUseStepKeyworkAndNameForChildName() throws Exception {
        List<PickleEvent> pickleEvents = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
                mock(RunnerSupplier.class),
                pickleEvents.get(0),
                createJUnitOptions()
        );

        assertEquals("it works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    @Test
    public void shouldConvertTextFromFeatureFileForNamesWithFilenameCompatibleNameOption() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
                mock(RunnerSupplier.class),
                pickles.get(0),
                createJunitOptions("--filename-compatible-names")
        );

        assertEquals("scenario_name", runner.getDescription().getDisplayName());
        assertEquals("scenario_name", runner.getDescription().getChildren().get(0).getClassName());
        assertEquals("it_works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    private JUnitOptions createJUnitOptions() {
        return new JUnitOptions(true, Collections.<String>emptyList());
    }

    private JUnitOptions createJunitOptions(String option) {
        return new JUnitOptions(true, Arrays.asList(option));
    }
}
