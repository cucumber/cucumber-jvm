package cucumber.runtime.junit;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ScenarioOutline;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class ScenarioOutlineRunner extends ParentRunner<ExamplesRunner> {
    private final ScenarioOutline scenarioOutline;
    private List<ExamplesRunner> children;

    public ScenarioOutlineRunner(Runtime runtime, ScenarioOutline scenarioOutline) throws InitializationError {
        super(null);
        this.scenarioOutline = scenarioOutline;
        children = new ArrayList<ExamplesRunner>();
        for (Examples examples : scenarioOutline.getExamples()) {
            try {
                children.add(new ExamplesRunner(runtime, examples));
            } catch (InitializationError initializationError) {
                throw new CucumberException("Should never happen", initializationError);
            }
        }
    }

    @Override
    public String getName() {
        return scenarioOutline.getKeyword() + ": " + scenarioOutline.getName();
    }

    @Override
    protected List<ExamplesRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(ExamplesRunner runner) {
        return runner.getDescription();
    }

    @Override
    protected void runChild(ExamplesRunner runner, RunNotifier notifier) {
        runner.run(notifier);
    }
}
