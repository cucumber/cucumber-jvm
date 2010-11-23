package cucumber;

import cucumber.runtime.*;
import gherkin.FeatureParser;
import gherkin.GherkinParser;
import gherkin.formatter.Formatter;

import java.io.IOException;
import java.util.List;

/**
 * A high level façade for running Cucumber features.
 */
public class Cucumber {
    private final FeatureParser parser;

    public Cucumber(Backend backend, Formatter formatter) {
        ExecuteFormatter executeFormatter = new ExecuteFormatter(backend, formatter);
        parser = new GherkinParser(executeFormatter);
    }

    public void execute(List<String> paths) throws IOException {
        for (String path : paths) {
            // TODO: Check for :line:line.
            // TODO: Make Classpath.scan deal with both files and dirs. Tests!!!

            Classpath.scan(path, ".feature", new Consumer() {
                public void consume(Input input) throws IOException {
                    parser.parse(input.getString(), input.getPath(), 0);
                }
            });
        }
    }
}
