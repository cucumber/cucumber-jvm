package cucumber.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenarioOutline;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class ScenarioOutlineRunner extends ParentRunner<ExamplesRunner> {
    private final CucumberScenarioOutline cucumberScenarioOutline;
    private List<ExamplesRunner> children = new ArrayList<ExamplesRunner>();

    public ScenarioOutlineRunner(Runtime runtime, List<String> extraCodePaths, CucumberScenarioOutline cucumberScenarioOutline, JUnitReporter jUnitReporter) throws InitializationError {
        super(null);
        this.cucumberScenarioOutline = cucumberScenarioOutline;
        for (CucumberExamples cucumberExamples : cucumberScenarioOutline.getCucumberExamplesList()) {
            children.add(new ExamplesRunner(runtime, extraCodePaths, cucumberExamples, jUnitReporter));
        }
    }

    @Override
    public String getName() {
        return cucumberScenarioOutline.getKeywordAndName();
    }

    @Override
    protected List<ExamplesRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(ExamplesRunner examplesRunner) {
        return examplesRunner.getDescription();
    }

    @Override
    protected void runChild(ExamplesRunner examplesRunner, RunNotifier notifier) {
        examplesRunner.run(notifier);
    }
}
