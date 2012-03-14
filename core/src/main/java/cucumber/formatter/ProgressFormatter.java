package cucumber.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.ansi.AnsiEscapes;
import gherkin.formatter.model.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressFormatter implements Formatter, Reporter {
    private static final Map<String, Character> CHARS = new HashMap<String, Character>() {{
        put("passed", '.');
        put("undefined", 'U');
        put("pending", 'P');
        put("skipped", '-');
        put("failed", 'F');
    }};
    private static final Map<String, AnsiEscapes> ANSI_ESCAPES = new HashMap<String, AnsiEscapes>() {{
        put("passed", AnsiEscapes.GREEN);
        put("undefined", AnsiEscapes.YELLOW);
        put("pending", AnsiEscapes.YELLOW);
        put("skipped", AnsiEscapes.CYAN);
        put("failed", AnsiEscapes.RED);
    }};

    private final NiceAppendable out;
    private final boolean monochrome;

    public ProgressFormatter(Appendable appendable) {
        this.monochrome = false;
        out = new NiceAppendable(appendable);
    }

    @Override
    public void uri(String uri) {
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
        out.println();
    }

    @Override
    public void close() {
        out.close();
    }

    @Override
    public void result(Result result) {
        if (!monochrome) {
            ANSI_ESCAPES.get(result.getStatus()).appendTo(out);
        }
        out.append(CHARS.get(result.getStatus()));
        if (!monochrome) {
            AnsiEscapes.RESET.appendTo(out);
        }
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
