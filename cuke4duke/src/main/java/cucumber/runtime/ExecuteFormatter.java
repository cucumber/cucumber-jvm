package cucumber.runtime;

import cucumber.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.List;

public class ExecuteFormatter implements Formatter {
    private final PrettyFormatter formatter;
    private final cucumber.Runtime runtime;

    public ExecuteFormatter(Runtime runtime, PrettyFormatter formatter) {
        this.runtime = runtime;
        this.formatter = formatter;
    }

    public void uri(String uri) {
        formatter.uri(uri);
    }

    public void feature(Feature feature) {
        formatter.feature(feature);
    }

    public void background(Background background) {
        formatter.background(background);
    }

    public void scenario(Scenario scenario) {
        runtime.replay();
        runtime.featureElement(scenario);
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        formatter.scenarioOutline(scenarioOutline);
    }

    public void examples(Examples examples) {
        formatter.examples(examples);
    }

    public void step(Step step) {
        runtime.step(step);
    }

    public void eof() {
        runtime.replay();
        formatter.eof();
    }

    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
        formatter.syntaxError(state, event, legalEvents, uri, line);
    }
}
