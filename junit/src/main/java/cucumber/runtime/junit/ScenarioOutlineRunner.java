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
    private final Description description;

    public ScenarioOutlineRunner(Class<?> testClass, Runtime runtime, CucumberScenarioOutline cucumberScenarioOutline, JUnitReporter jUnitReporter, String uri) throws InitializationError {
        super(testClass, new ArrayList<Runner>());
        // In order for the eclipse integration to work properly, the name of the description has to be unique
        // using the uniqueId of the Description will not work because when running a single test a new description is
        // created based on the the name of the selected description.
        this.description = Description.createSuiteDescription(cucumberScenarioOutline.getVisualName() + " -- " + uri + ":" + cucumberScenarioOutline.getGherkinModel().getLine());
        for (CucumberExamples cucumberExamples : cucumberScenarioOutline.getCucumberExamplesList()) {
            ExamplesRunner child = new ExamplesRunner(testClass, runtime, cucumberExamples, jUnitReporter, uri);
            getChildren().add(child);
            description.addChild(describeChild(child));
        }
    }

    @Override
    public String getName() {
        return description.getDisplayName();
    }

    @Override
    public Description getDescription() {
        return description;
    }
    
}
