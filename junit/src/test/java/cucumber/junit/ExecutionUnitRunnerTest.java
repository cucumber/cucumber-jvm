package cucumber.junit;

import static org.junit.Assert.*;

import gherkin.formatter.model.Step;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;

public class ExecutionUnitRunnerTest {
    @Test
    public void shouldAssignUnequalDescriptionsToDifferentOccurrencesOfSameStepInAScenario() throws Exception {
        List<CucumberFeature> features =
            CucumberFeature.load(
                new ClasspathResourceLoader(this.getClass().getClassLoader()),
                Arrays.asList(new String[] { "cucumber/junit/feature_with_same_steps_in_scenario.feature" }), 
                Collections.emptyList());
        
        ExecutionUnitRunner runner = 
            new ExecutionUnitRunner(
                    null, 
                    (CucumberScenario)features.get(0).getFeatureElements().get(0), 
                    null);
        
        // fish out the two occurrences of the same step and check whether we really got them
        Step stepOccurrence1 = runner.getChildren().get(0);
        Step stepOccurrence2 = runner.getChildren().get(2);
        assertEquals(stepOccurrence1.getName(), stepOccurrence2.getName());
        
        assertFalse("Descriptions must not be equal.",
                runner.describeChild(stepOccurrence1)
                    .equals(runner.describeChild(stepOccurrence2)));
    }
}
