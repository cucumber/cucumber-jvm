package cucumber;

import cucumber.runtime.*;
import gherkin.FeatureParser;
import gherkin.GherkinParser;
import gherkin.formatter.Reporter;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * A high level façade for running Cucumber features.
 */
public class Cucumber {
    private final List<Backend> backends;
    private final Reporter reporter;
    private final SummaryReporter summaryReporter;

    public Cucumber(List<Backend> backends, Reporter reporter, SummaryReporter summaryReporter) {
        this.backends = backends;
        this.reporter = reporter;
        this.summaryReporter = summaryReporter;
    }

    public Cucumber(Backend backend, Reporter reporter, SummaryReporter summaryReporter) {
        this(asList(backend), reporter, summaryReporter);
    }

    public void execute(String... paths) {
        ExecuteFormatter executeFormatter = new ExecuteFormatter(backends, reporter);
        final FeatureParser parser = new GherkinParser(executeFormatter);
        for (String path : paths) {
            // TODO: Check for :line:line.
            // TODO: Make Classpath.scan deal with both files and dirs. Tests!!!

            Classpath.scan(path, ".feature", new Consumer() {
                public void consume(Input input) {
                    parser.parse(input.getString(), input.getPath(), 0);
                }
            });
        }
        executeFormatter.reportSummary(summaryReporter);
    }
}
