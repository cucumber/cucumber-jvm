package cucumber.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenario;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

class ExamplesRunner extends Suite {
    private final CucumberExamples cucumberExamples;

    protected ExamplesRunner(Runtime runtime, List<String> gluePaths, CucumberExamples cucumberExamples, JUnitReporter jUnitReporter) throws InitializationError {
        super(null, new ArrayList<Runner>());
        this.cucumberExamples = cucumberExamples;

        //TODO: I will have to create a world here, this is the entry point for junit I think

        List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
        for (CucumberScenario scenario : exampleScenarios) {
            try {
                ExecutionUnitRunner exampleScenarioRunner = new ExecutionUnitRunner(runtime, scenario, jUnitReporter);
                getChildren().add(exampleScenarioRunner);
            } catch (InitializationError initializationError) {
                initializationError.printStackTrace();
            }
        }
    }

    @Override
    protected String getName() {
        return cucumberExamples.getExamples().getKeyword() + ": " + cucumberExamples.getExamples().getName();
    }
}
