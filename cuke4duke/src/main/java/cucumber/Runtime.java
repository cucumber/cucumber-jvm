package cucumber;

import cucumber.runtime.ExecuteFormatter;
import cucumber.runtime.StepMatch;
import gherkin.FeatureParser;
import gherkin.GherkinParser;
import gherkin.formatter.Argument;
import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.BasicStatement;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Runtime {
    private final List<StepDefinition> stepDefinitions;
    private final PrettyFormatter formatter;
    private final FeatureParser parser;
    private final List<Step> steps = new ArrayList<Step>();

    private BasicStatement featureElement;
    private String uri;
    private Feature feature;

    public Runtime(List<StepDefinition> stepDefinitions, PrettyFormatter formatter) {
        this.stepDefinitions = stepDefinitions;
        this.formatter = formatter;
        ExecuteFormatter executeFormatter = new ExecuteFormatter(this, formatter);
        parser = new GherkinParser(executeFormatter);
    }

    public void execute(FeatureSource featureSource) {
        featureSource.execute(this);
    }

    public void execute(String source, String location) {
        parser.parse(source, location, 0);
    }

    public void uri(String uri) {
        this.uri = uri;
    }


    public void feature(Feature feature) {
        this.feature = feature;
    }

    public void featureElement(BasicStatement featureElement) {
        this.featureElement = featureElement;
    }

    public void step(Step step) {
        steps.add(step);
    }
    
    public void replay() {
        if(featureElement != null) {
            List<List<String>> stepStrings = new ArrayList<List<String>>();
            for(Step step: steps) {
                stepStrings.add(Arrays.asList(step.getKeyword(), step.getName()));
            }
            formatter.steps(stepStrings);

            featureElement.replay(formatter);
            for(Step step: steps) {
                List<Argument> arguments = Arrays.asList(new Argument(7, "3"));
                new StepMatch(stepDefinitions.get(0), arguments, step, uri, feature.getName(), featureElement.getName()).execute(formatter);
            }
        }
    }
}
