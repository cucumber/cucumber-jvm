package cucumber.junit;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

class RunnerBuilder implements Formatter {
    private final Runtime runtime;
    private final List<ParentRunner> children;
    private List<ScenarioRunner> scenarioRunners = new ArrayList<ScenarioRunner>();
    private Feature feature;
    private ScenarioRunner scenarioRunner;

    public RunnerBuilder(Runtime runtime, List<ParentRunner> children) {
        this.runtime = runtime;
        this.children = children;
    }

    public void uri(String uri) {
    }

    public void feature(Feature feature) {
        this.feature = feature;
    }

    public void background(Background background) {
    }

    public void scenario(Scenario scenario) {
        try {
            scenarioRunner = new ScenarioRunner(runtime, scenario);
            children.add(scenarioRunner);
        } catch (InitializationError initializationError) {
            throw new CucumberException("Failed to create runner", initializationError);
        }
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    public void examples(Examples examples) {
    }

    public void step(Step step) {
        scenarioRunner.step(step);
    }

    public void eof() {
    }

    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
    }

    public Feature getFeature() {
        return feature;
    }

    public List<ScenarioRunner> getScenarioRunners() {
        return scenarioRunners;
    }
}
