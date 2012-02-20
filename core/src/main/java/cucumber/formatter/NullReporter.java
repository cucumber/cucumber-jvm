package cucumber.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.io.InputStream;
import java.util.List;

public class NullReporter implements Reporter, Formatter {
    @Override
    public void uri(String s) {
    }

    @Override
    public void feature(Feature feature) {
    }

    @Override
    public void background(Background background) {
    }

    @Override
    public void scenario(Scenario scenario) {
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void step(Step step) {
    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
    }

    @Override
    public void done() {
    }

    @Override
    public void close() {
    }

    @Override
    public void result(Result result) {
    }

    @Override
    public void match(Match match) {
    }

    @Override
    public void embedding(String mimeType, InputStream data) {
    }

    @Override
    public void write(String text) {
    }
}
