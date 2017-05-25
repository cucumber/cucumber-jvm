package cucumber.runtime.junit;

import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class ExecutionUnitRunnerTest {

    @Test
    public void shouldAssignUnequalDescriptionsToDifferentOccurrencesOfSameStepInAScenario() throws Exception {
        List<CucumberFeature> features = CucumberFeature.load(
                new ClasspathResourceLoader(this.getClass().getClassLoader()),
                asList("cucumber/runtime/junit/fb.feature"),
                null
        );
        Compiler compiler = new Compiler();
        List<PickleEvent> pickleEvents = new ArrayList<PickleEvent>();
        for (Pickle pickle : compiler.compile(features.get(0).getGherkinFeature())) {
            pickleEvents.add(new PickleEvent(features.get(0).getPath(), pickle));
        };

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                mock(Runner.class),
                pickleEvents.get(0),
                createStandardJUnitReporter()
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
    public void shouldIncludeScenarioNameAsClassNameInStepDescriptions() throws Exception {
        List<CucumberFeature> features = CucumberFeature.load(
                new ClasspathResourceLoader(this.getClass().getClassLoader()),
                asList("cucumber/runtime/junit/feature_with_same_steps_in_different_scenarios.feature"),
                null
        );
        Compiler compiler = new Compiler();
        List<PickleEvent> pickleEvents = new ArrayList<PickleEvent>();
        for (Pickle pickle : compiler.compile(features.get(0).getGherkinFeature())) {
            pickleEvents.add(new PickleEvent(features.get(0).getPath(), pickle));
        };

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                mock(Runner.class),
                pickleEvents.get(0),
                createStandardJUnitReporter()
        );

        // fish out the data from runner
        PickleStep step = runner.getChildren().get(0);
        Description runnerDescription = runner.getDescription();
        Description stepDescription = runnerDescription.getChildren().get(0);

        assertEquals("description includes scenario name as class name", runner.getName(), stepDescription.getClassName());
        assertEquals("description includes step keyword and name as method name", step.getText(), stepDescription.getMethodName());
    }

    @Test
    public void shouldUseScenarioNameForRunnerName() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                mock(Runner.class),
                pickles.get(0),
                createStandardJUnitReporter()
        );

        assertEquals("scenario name", runner.getName());
    }

    @Test
    public void shouldUseStepKeyworkAndNameForChildName() throws Exception {
        List<PickleEvent> pickleEvents = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                mock(Runner.class),
                pickleEvents.get(0),
                createStandardJUnitReporter()
        );

        assertEquals("it works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    @Test
    public void shouldConvertTextFromFeatureFileForNamesWithFilenameCompatibleNameOption() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                mock(Runner.class),
                pickles.get(0),
                createJUnitReporterWithOption("--filename-compatible-names")
        );

        assertEquals("scenario_name", runner.getName());
        assertEquals("it_works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    private JUnitReporter createStandardJUnitReporter() {
        return new JUnitReporter(mock(EventBus.class), false, new JUnitOptions(Collections.<String>emptyList()));
    }

    private JUnitReporter createJUnitReporterWithOption(String option) {
        return new JUnitReporter(mock(EventBus.class), false, new JUnitOptions(Arrays.asList(option)));
    }
}
