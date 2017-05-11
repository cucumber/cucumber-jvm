package cucumber.runtime.junit;

import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;
import org.junit.Test;
import org.junit.runner.Description;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ExecutionUnitRunnerTest {
    @Test
    public void shouldAssignUnequalDescriptionsToDifferentOccurrencesOfSameStepInAScenario() throws Exception {
        List<CucumberFeature> features = CucumberFeature.load(
                new ClasspathResourceLoader(this.getClass().getClassLoader()),
                asList("cucumber/runtime/junit/fb.feature"),
                Collections.emptyList()
        );

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                null,
                (CucumberScenario) features.get(0).getFeatureElements().get(0),
                createStandardJUnitReporter(),
                createStandardIdGenerator()
        );

        // fish out the two occurrences of the same step and check whether we really got them
        Step stepOccurrence1 = runner.getChildren().get(0);
        Step stepOccurrence2 = runner.getChildren().get(2);
        assertEquals(stepOccurrence1.getName(), stepOccurrence2.getName());

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
                Collections.emptyList()
        );

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                null,
                (CucumberScenario) features.get(0).getFeatureElements().get(0),
                createStandardJUnitReporter(),
                createStandardIdGenerator()
        );

        // fish out the data from runner
        Step step = runner.getChildren().get(0);
        Description runnerDescription = runner.getDescription();
        Description stepDescription = runnerDescription.getChildren().get(0);

        assertEquals("description includes scenario name as class name", runner.getName(), stepDescription.getClassName());
        assertEquals("description includes step keyword and name as method name", step.getKeyword() + step.getName(), stepDescription.getMethodName());
    }


    @Test
    public void shouldUseScenarioNameForRunnerName() throws Exception {
        CucumberFeature cucumberFeature = TestFeatureBuilder.feature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                null,
                (CucumberScenario) cucumberFeature.getFeatureElements().get(0),
                createStandardJUnitReporter(),
                createStandardIdGenerator()
        );

        assertEquals("Scenario: scenario name", runner.getName());
    }

    @Test
    public void shouldUseStepKeyworkAndNameForChildName() throws Exception {
        CucumberFeature cucumberFeature = TestFeatureBuilder.feature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                null,
                (CucumberScenario) cucumberFeature.getFeatureElements().get(0),
                createStandardJUnitReporter(),
                createStandardIdGenerator()
        );

        assertEquals("Then it works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    @Test
    public void shouldConvertTextFromFeatureFileForNamesWithFilenameCompatibleNameOption() throws Exception {
        CucumberFeature cucumberFeature = TestFeatureBuilder.feature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        ExecutionUnitRunner runner = new ExecutionUnitRunner(
                null,
                (CucumberScenario) cucumberFeature.getFeatureElements().get(0),
                createJUnitReporterWithOption("--filename-compatible-names"),
                createStandardIdGenerator()
        );

        assertEquals("Scenario__scenario_name", runner.getName());
        assertEquals("Then_it_works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    private JUnitReporter createStandardJUnitReporter() {
        return new JUnitReporter(null, null, false, new JUnitOptions(Collections.<String>emptyList()));
    }

    private JUnitReporter createJUnitReporterWithOption(String option) {
        return new JUnitReporter(null, null, false, new JUnitOptions(Arrays.asList(option)));
    }

    private IdProvider createStandardIdGenerator() {
        return new IdProvider("dummy base");
    }
}
