package cucumber.runtime;

import cucumber.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.DescribedStatement;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExecuteFormatter implements Formatter {
    private final List<StepDefinition> stepDefinitions;
    private final Formatter formatter;
    private String uri;
    private Feature feature;
    private DescribedStatement featureElement;
    private final List<Step> steps = new ArrayList<Step>();

    public ExecuteFormatter(List<StepDefinition> stepDefinitions, Formatter formatter) {
        this.stepDefinitions = stepDefinitions;
        this.formatter = formatter;
    }

    public void uri(String uri) {
        this.uri = uri;
        formatter.uri(uri);
    }

    public void feature(Feature feature) {
        this.feature = feature;
        formatter.feature(feature);
    }

    public void background(Background background) {
        formatter.background(background);
    }

    public void scenario(Scenario scenario) {
        replayFeatureElement();
        featureElement = scenario;
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        replayFeatureElement();
        featureElement = scenarioOutline;
    }

    public void examples(Examples examples) {
        formatter.examples(examples);
    }

    public void step(Step step) {
        steps.add(step);
    }

    public void eof() {
        replayFeatureElement();
        formatter.eof();
    }

    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
        formatter.syntaxError(state, event, legalEvents, uri, line);
    }

    public void steps(List<Step> steps) {
        throw new UnsupportedOperationException();
    }

    private void replayFeatureElement() {
        if(featureElement != null) {
            formatter.steps(steps);
            featureElement.replay(formatter);
            for(Step step: steps) {
                List<Argument> arguments = Arrays.asList(new Argument(7, "3"));
                new StepMatch(stepDefinitions.get(0), arguments, step, uri, feature.getName(), featureElement.getName()).execute(formatter);
            }
            steps.clear();
        }
    }
}
