package cucumber.formatter;

import cucumber.runtime.CucumberException;
import gherkin.formatter.Formatter;
import gherkin.formatter.Mappable;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.json.simple.JSONValue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class HTMLFormatter implements Formatter, Reporter {

    private final NiceAppendable out;
    private static final String JS_FORMATTER_VAR = "formatter";
    private static final String JS_REPORT_FILENAME = "report.js";

    public HTMLFormatter() {
        try {
            out = new NiceAppendable(new BufferedWriter(new FileWriter(JS_REPORT_FILENAME)));
        } catch (IOException e) {
            throw new CucumberException("Unable to create javascript report file: " + JS_REPORT_FILENAME, e);
        }
    }

    public HTMLFormatter(Appendable appendable) {
        out = new NiceAppendable(appendable);
    }

    @Override
    public void uri(String uri) {
        writeToJsReport("uri", "'" + uri + "'");
    }

    @Override
    public void feature(Feature feature) {
        writeToJsReport("feature", feature);
    }

    @Override
    public void background(Background background) {
        writeToJsReport("background", background);
    }

    @Override
    public void scenario(Scenario scenario) {
        writeToJsReport("scenario", scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        writeToJsReport("scenarioOutline", scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
        writeToJsReport("examples", examples);
    }

    @Override
    public void step(Step step) {
        writeToJsReport("step", step);
    }

    @Override
    public void eof() {
        //
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
        //
    }

    private void writeToJsReport(String functionName, Mappable statement) {

        writeToJsReport(functionName, JSONValue.toJSONString(statement.toMap()).replaceAll("\\'", "\\\\'"));
    }

    private void writeToJsReport(String functionName, String arg) {
        out.append(JS_FORMATTER_VAR + ".").append(functionName).append("(").append(arg).append(");").println();
    }

    @Override
    public void result(Result result) {
        writeToJsReport("result", result);
    }

    @Override
    public void match(Match match) {
        writeToJsReport("match", match);
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        // TODO Treat embedded data
    }

}
