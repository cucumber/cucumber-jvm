package cucumber;

import cucumber.runtime.ExecuteFormatter;
import gherkin.FeatureParser;
import gherkin.GherkinParser;
import gherkin.formatter.PrettyFormatter;

import java.util.List;

public class Runtime {
    private final FeatureParser parser;

    public Runtime(List<StepDefinition> stepDefinitions, PrettyFormatter formatter) {
        ExecuteFormatter executeFormatter = new ExecuteFormatter(stepDefinitions, formatter);
        parser = new GherkinParser(executeFormatter);
    }

    public void execute(FeatureSource featureSource) {
        featureSource.execute(this);
    }

    public void execute(String source, String location) {
        parser.parse(source, location, 0);
    }
}
