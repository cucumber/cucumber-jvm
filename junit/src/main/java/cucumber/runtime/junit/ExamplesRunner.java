package cucumber.runtime.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenario;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

class ExamplesRunner extends Suite {
    private final Description description;

    protected ExamplesRunner(Class<?> testClass, Runtime runtime, CucumberExamples cucumberExamples, JUnitReporter jUnitReporter, String uri) throws InitializationError {
        super(testClass, new ArrayList<Runner>());
        this.description = Description.createSuiteDescription(uniqueName(cucumberExamples, uri));
        List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
        for (CucumberScenario scenario : exampleScenarios) {
            try {
                ExecutionUnitRunner exampleScenarioRunner = new ExecutionUnitRunner(testClass, runtime, scenario, jUnitReporter, uri);
                getChildren().add(exampleScenarioRunner);
                description.addChild(describeChild(exampleScenarioRunner));
            } catch (InitializationError initializationError) {
                initializationError.printStackTrace();
            }
        }
    }

    private String uniqueName(CucumberExamples cucumberExamples, String uri) {
        return cucumberExamples.getExamples().getKeyword() + ": " + cucumberExamples.getExamples().getName() + " -- " + uri + ":" + cucumberExamples.getExamples().getLine();
    }

    @Override
    protected String getName() {
        return description.getDisplayName();
    }

    @Override
    public Description getDescription() {
        return description;
    }
}
