package cucumber.runtime.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenarioOutline;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class ScenarioOutlineRunner extends Suite {
    private final CucumberScenarioOutline cucumberScenarioOutline;
    private final JUnitReporter jUnitReporter;
    private Description description;

    public ScenarioOutlineRunner(Runtime runtime, CucumberScenarioOutline cucumberScenarioOutline, JUnitReporter jUnitReporter) throws InitializationError {
        super(null, buildRunners(runtime, cucumberScenarioOutline, jUnitReporter));
        this.cucumberScenarioOutline = cucumberScenarioOutline;
        this.jUnitReporter = jUnitReporter;
    }

    private static List<Runner> buildRunners(Runtime runtime, CucumberScenarioOutline cucumberScenarioOutline, JUnitReporter jUnitReporter) throws InitializationError {
        List<Runner> runners = new ArrayList<Runner>();
        for (CucumberExamples cucumberExamples : cucumberScenarioOutline.getCucumberExamplesList()) {
            runners.add(new ExamplesRunner(runtime, cucumberExamples, jUnitReporter));
        }
        return runners;
    }

    @Override
    public String getName() {
        return cucumberScenarioOutline.getVisualName();
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), cucumberScenarioOutline.getGherkinModel());
            for (Runner child : getChildren()) {
                description.addChild(describeChild(child));
            }
        }
        return description;
    }

    @Override
    public void run(final RunNotifier notifier) {
        cucumberScenarioOutline.formatOutlineScenario(jUnitReporter);
        super.run(notifier);
    }
}
