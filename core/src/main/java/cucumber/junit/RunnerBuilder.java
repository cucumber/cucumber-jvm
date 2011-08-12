package cucumber.junit;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class RunnerBuilder implements Formatter {
    private final Runtime runtime;
    private final List<ScenarioRunner> children;
    private gherkin.formatter.model.Feature feature;
    private ScenarioRunner scenarioRunner;

    public RunnerBuilder(Runtime runtime, List<ScenarioRunner> children) {
        this.runtime = runtime;
        this.children = children;
    }

    @Override
    public void uri(String uri) {
    }

    @Override
    public void feature(gherkin.formatter.model.Feature feature) {
        this.feature = feature;
    }

    @Override
    public void background(Background background) {
    }

    @Override
    public void scenario(Scenario scenario) {
        try {
            scenarioRunner = new ScenarioRunner(runtime, scenario);
            children.add(scenarioRunner);
        } catch (InitializationError initializationError) {
            throw new CucumberException("Failed to create runner", initializationError);
        }
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void step(Step step) {
        scenarioRunner.step(step);
    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
    }

    public gherkin.formatter.model.Feature getFeature() {
        return feature;
    }
}
