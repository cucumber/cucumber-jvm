package cucumber.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenario;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class ExamplesRunner extends ParentRunner<ExecutionUnitRunner> {
    private final CucumberExamples cucumberExamples;
    private final List<ExecutionUnitRunner> children = new ArrayList<ExecutionUnitRunner>();

    protected ExamplesRunner(Runtime runtime, List<String> extraCodePaths, CucumberExamples cucumberExamples, JUnitReporter jUnitReporter) throws InitializationError {
        super(null);
        this.cucumberExamples = cucumberExamples;

        List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
        for (CucumberScenario scenario : exampleScenarios) {
            try {
                ExecutionUnitRunner exampleScenarioRunner = new ExecutionUnitRunner(runtime, extraCodePaths, scenario, jUnitReporter);
                children.add(exampleScenarioRunner);
            } catch (InitializationError initializationError) {
                initializationError.printStackTrace();
            }
        }
    }

    @Override
    protected List<ExecutionUnitRunner> getChildren() {
        return children;
    }

    @Override
    protected String getName() {
        return cucumberExamples.getExamples().getKeyword() + ": " + cucumberExamples.getExamples().getName();
    }

    @Override
    protected Description describeChild(ExecutionUnitRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(ExecutionUnitRunner child, RunNotifier notifier) {
        child.run(notifier);
    }
}
