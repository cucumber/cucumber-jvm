package cucumber.runtime;

import cucumber.StepDefinition;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.DescribedStatement;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

public class ExecuteFormatter implements Formatter {
    private final Backend backend;
    private final List<StepDefinition> stepDefinitions;
    private final Formatter formatter;
    private final List<Step> steps = new ArrayList<Step>();

    private String uri;
    private Feature feature;
    private DescribedStatement featureElement;

    public ExecuteFormatter(Backend backend, List<StepDefinition> stepDefinitions, Formatter formatter) {
        this.backend = backend;
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
            backend.newScenario();
            formatter.steps(steps);
            featureElement.replay(formatter);
            for(Step step: steps) {
                execute(step);
            }
            steps.clear();
        }
    }

    private void execute(Step step) {
        StepMatch stepMatch = stepMatch(step);
        StackTraceElement stepStackTraceElement = new StackTraceElement(feature.getName() + "." + featureElement.getName(), step.getKeyword()+step.getName(), uri, step.getLine());
        stepMatch.execute(formatter, stepStackTraceElement);
    }

    private StepMatch stepMatch(Step step) {
        List<StepMatch> stepMatches = stepMatches(step);
        return stepMatches.get(0);
    }

    private List<StepMatch> stepMatches(Step step) {
        List<StepMatch> result = new ArrayList<StepMatch>();
        for(StepDefinition stepDefinition : stepDefinitions) {
            StepMatch stepMatch = stepDefinition.stepMatch(step);
            if(stepMatch != null) {
                result.add(stepMatch);
            }
        }
        return result;
    }
}
