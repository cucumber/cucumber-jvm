package cucumber.runtime;

import cucumber.FeatureSource;
import gherkin.FeatureParser;
import gherkin.GherkinParser;
import gherkin.formatter.Formatter;

public class Executor {
    private final FeatureParser parser;

    public Executor(Backend backend, Formatter formatter) {
        ExecuteFormatter executeFormatter = new ExecuteFormatter(backend, formatter);
        parser = new GherkinParser(executeFormatter);
    }

    public void execute(FeatureSource featureSource) {
        featureSource.execute(parser);
    }
}
