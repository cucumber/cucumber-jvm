package cucumber.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;
import org.junit.Ignore;

import java.io.File;
import java.util.List;

@Ignore
public class TestFormatter implements Formatter {
    public Appendable appendable;
    public File dir;

    public TestFormatter(Appendable appendable) {
        this.appendable = appendable;
    }

    public TestFormatter(File dir) {
        this.dir = dir;
    }

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
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
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
