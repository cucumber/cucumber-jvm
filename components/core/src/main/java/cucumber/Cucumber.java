package cucumber;

import cucumber.runtime.*;
import gherkin.FeatureParser;
import gherkin.GherkinParser;
import gherkin.formatter.Reporter;

import java.util.List;

/**
 * A high level façade for running Cucumber features.
 */
public class Cucumber {
    private final FeatureParser parser;

    public Cucumber(List<Backend> backends, Reporter reporter) {
        ExecuteFormatter executeFormatter = new ExecuteFormatter(backends, reporter);
        parser = new GherkinParser(executeFormatter);
    }

    public void execute(String... paths) {
        for (String path : paths) {
            // TODO: Check for :line:line.
            // TODO: Make Classpath.scan deal with both files and dirs. Tests!!!

            Classpath.scan(path, ".feature", new Consumer() {
                public void consume(Input input) {
                    parser.parse(input.getString(), input.getPath(), 0);
                }
            });
        }
    }
}
