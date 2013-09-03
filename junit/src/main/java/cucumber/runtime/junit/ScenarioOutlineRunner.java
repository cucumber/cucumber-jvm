package cucumber.runtime.junit;

import java.util.ArrayList;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenarioOutline;

class ScenarioOutlineRunner extends Suite {
    private final CucumberScenarioOutline cucumberScenarioOutline;
    private final Description description;

    public ScenarioOutlineRunner(Class<?> testClass, Runtime runtime, CucumberScenarioOutline cucumberScenarioOutline, JUnitReporter jUnitReporter, String uri) throws InitializationError {
        super(testClass, new ArrayList<Runner>());
        this.cucumberScenarioOutline = cucumberScenarioOutline;
        this.description = Description.createSuiteDescription(getName(), uri);
        for (CucumberExamples cucumberExamples : cucumberScenarioOutline.getCucumberExamplesList()) {
            ExamplesRunner child = new ExamplesRunner(testClass, runtime, cucumberExamples, jUnitReporter, uri);
            getChildren().add(child);
            description.addChild(describeChild(child));
        }
    }

    @Override
    public String getName() {
        return cucumberScenarioOutline.getVisualName();
    }

    @Override
    public Description getDescription() {
        return description;
    }
    
}
