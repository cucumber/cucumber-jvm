package cucumber.runtime.android;

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

import java.util.List;

/**
 * A "no operation" abstract implementation of the {@link Formatter} and {@link Reporter}
 * interface to ease overriding only specific methods.
 */
abstract class NoOpFormattingReporter implements Formatter, Reporter {
    
    @Override
    public void uri(String uri) {
        // NoOp
    }

    @Override
    public void feature(Feature feature) {
        // NoOp
    }

    @Override
    public void background(Background background) {
        // NoOp
    }

    @Override
    public void scenario(Scenario scenario) {
        // NoOp
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        // NoOp
    }

    @Override
    public void examples(Examples examples) {
        // NoOp
    }

    @Override
    public void step(Step step) {
        // NoOp
    }

    @Override
    public void eof() {
        // NoOp
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        // NoOp
    }

    @Override
    public void done() {
        // NoOp
    }

    @Override
    public void close() {
        // NoOp
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        // NoOp
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        // NoOp
    }

    @Override
    public void before(Match match, Result result) {
        // NoOp
    }

    @Override
    public void result(Result result) {
        // NoOp
    }

    @Override
    public void after(Match match, Result result) {
        // NoOp
    }

    @Override
    public void match(Match match) {
        // NoOp
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        // NoOp
    }

    @Override
    public void write(String text) {
        // NoOp
    }
}
