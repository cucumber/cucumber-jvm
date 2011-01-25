package cucumber.runtime.junit;

import cucumber.runtime.*;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Scenario;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class ExamplesRunner extends ParentRunner<ScenarioRunner> {
    private final Examples examples;
    private List<ScenarioRunner> children;

    public ExamplesRunner(cucumber.runtime.Runtime runtime, Examples examples) throws InitializationError {
        super(null);
        this.examples = examples;

        children = new ArrayList<ScenarioRunner>();
        for (Scenario scenario : examples.getScenarios()) {
            try {
                children.add(new ScenarioRunner(runtime, scenario));
            } catch (InitializationError initializationError) {
                throw new CucumberException("This should never happen", initializationError);
            }
        }
    }
    
    @Override
    public String getName() {
        return examples.getKeyword() + ": " + examples.getName();
    }

    @Override
    protected List<ScenarioRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(ScenarioRunner runner) {
        return runner.getDescription();
    }

    @Override
    protected void runChild(ScenarioRunner runner, RunNotifier notifier) {
        runner.run(notifier);
    }
}
