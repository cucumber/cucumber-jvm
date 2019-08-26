package cucumber.runtime.formatter;

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


public class FormatterSpy implements Formatter, Reporter {
    StringBuilder calls = new StringBuilder();

    @Override
    public void after(Match arg0, Result arg1) {
        calls.append("after\n");
    }

    @Override
    public void before(Match arg0, Result arg1) {
        calls.append("before\n");
    }

    @Override
    public void embedding(String arg0, byte[] arg1) {
        calls.append("      embedding\n");
    }

    @Override
    public void match(Match arg0) {
        calls.append("    match\n");
    }

    @Override
    public void result(Result arg0) {
        calls.append("    result\n");
    }

    @Override
    public void write(String arg0) {
        calls.append("      write\n");
    }

    @Override
    public void background(Background arg0) {
        calls.append("  background\n");
    }

    @Override
    public void close() {
        calls.append("close\n");
    }

    @Override
    public void done() {
        calls.append("done\n");
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario arg0) {
        calls.append("  endOfScenarioLifeCycle\n");
    }

    @Override
    public void eof() {
        calls.append("eof\n");
    }

    @Override
    public void examples(Examples arg0) {
        calls.append("  examples\n");
    }

    @Override
    public void feature(Feature arg0) {
        calls.append("feature\n");
    }

    @Override
    public void scenario(Scenario arg0) {
        calls.append("  scenario\n");
    }

    @Override
    public void scenarioOutline(ScenarioOutline arg0) {
        calls.append("  scenarioOutline\n");
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario arg0) {
        calls.append("  startOfScenarioLifeCycle\n");
    }

    @Override
    public void step(Step arg0) {
        calls.append("    step\n");
    }

    @Override
    public void syntaxError(String arg0, String arg1, List<String> arg2,
            String arg3, Integer arg4) {
        calls.append("syntaxError\n");
    }

    @Override
    public void uri(String arg0) {
        calls.append("uri\n");
    }

    @Override
    public String toString() {
        return calls.toString();
    }
}
