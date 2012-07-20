package cucumber.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.ansi.AnsiEscapes;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressFormatter implements Formatter, Reporter, ColorAware {
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
    private boolean monochrome = false;

    public ProgressFormatter(Appendable appendable) {
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
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
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
    public void before(Match match, Result result) {
        handleHook(match, result, "B");
    }

    @Override
    public void after(Match match, Result result) {
        handleHook(match, result, "A");
    }

    private void handleHook(Match match, Result result, String character) {
        if (result.getStatus().equals(Result.FAILED)) {
            if (!monochrome) {
                ANSI_ESCAPES.get(result.getStatus()).appendTo(out);
            }
            out.append(character);
            if (!monochrome) {
                AnsiEscapes.RESET.appendTo(out);
            }
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

    @Override
    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }
}
