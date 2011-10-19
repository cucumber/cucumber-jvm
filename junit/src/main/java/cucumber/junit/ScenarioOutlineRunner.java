package cucumber.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenarioOutline;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

class ScenarioOutlineRunner extends Suite {
    private final CucumberScenarioOutline cucumberScenarioOutline;

    public ScenarioOutlineRunner(Runtime runtime, List<String> codePaths, CucumberScenarioOutline cucumberScenarioOutline, JUnitReporter jUnitReporter) throws InitializationError {
        super(null, new ArrayList<Runner>());
        this.cucumberScenarioOutline = cucumberScenarioOutline;
        for (CucumberExamples cucumberExamples : cucumberScenarioOutline.getCucumberExamplesList()) {
            getChildren().add(new ExamplesRunner(runtime, codePaths, cucumberExamples, jUnitReporter));
        }
    }

    @Override
    public String getName() {
        return cucumberScenarioOutline.getVisualName();
    }
}
