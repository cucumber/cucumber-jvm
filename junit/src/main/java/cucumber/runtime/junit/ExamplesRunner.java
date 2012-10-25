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

import static cucumber.runtime.junit.DescriptionFactory.createDescription;

public class ExamplesRunner extends Suite {
    private final CucumberExamples cucumberExamples;
    private Description description;

    protected ExamplesRunner(Runtime runtime, CucumberExamples cucumberExamples, JUnitReporter jUnitReporter) throws InitializationError {
        super(ExamplesRunner.class, new ArrayList<Runner>());
        this.cucumberExamples = cucumberExamples;

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

    @Override
    public Description getDescription() {
        if (description == null) {
            description = createDescription(getName(), cucumberExamples);
            for (Runner child : getChildren()) {
                description.addChild(describeChild(child));
            }
        }
        return description;
    }
}
