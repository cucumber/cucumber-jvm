package cucumber.runtime.junit;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.Description;

import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import gherkin.formatter.model.Step;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ScenarioOutlineRunnerTest {
    @Test
    public void shouldIncludeExampleRowAsClassNameInStepDescriptions() throws Exception {
        List<CucumberFeature> features = CucumberFeature.load(
                new ClasspathResourceLoader(this.getClass().getClassLoader()),
                asList("cucumber/runtime/junit/feature_with_same_steps_in_different_scenarios.feature"),
                Collections.emptyList()
        );

        ScenarioOutlineRunner runner = new ScenarioOutlineRunner(
                null,
                (CucumberScenarioOutline) features.get(0).getFeatureElements().get(2),
                null
        );

        // fish out the data from runner
        ExecutionUnitRunner executionUnitRunner = ((ExecutionUnitRunner)((ExamplesRunner)(runner.getChildren().get(0))).getChildren().get(0));
        Step step = executionUnitRunner.getChildren().get(0);
        Description runnerDescription = executionUnitRunner.getDescription();
        Description stepDescription = runnerDescription.getChildren().get(0);

        System.out.println(runner.getDescription().getDisplayName());
        assertEquals(
                "description includes scenario outline keyword and example row as class name, and is properly escaped",
                "Scenario Outline: third.| {example} 1,2 |", stepDescription.getClassName());
        assertEquals("description includes step keyword and name as method name, and is properly escaped", "When {example} 1,2 {step}",
                stepDescription.getMethodName());
    }
}
