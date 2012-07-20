package cucumber.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.List;

public class StubFormatter implements Formatter {

    @Override
    public void uri(String uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void feature(Feature feature) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void background(Background background) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void scenario(Scenario scenario) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void examples(Examples examples) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void step(Step step) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void eof() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void done() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
}
