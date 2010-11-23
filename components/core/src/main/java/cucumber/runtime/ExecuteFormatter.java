package cucumber.runtime;

import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.List;

public class ExecuteFormatter implements Formatter {
    private final Backend backend;
    private final Formatter formatter;
    private final List<Step> steps = new ArrayList<Step>();

    private String uri;
    private Feature feature;
    private DescribedStatement featureElement;

    public ExecuteFormatter(Backend backend, Formatter formatter) {
        this.backend = backend;
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

    public void steps(List<Step> steps) {
        formatter.steps(steps);
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

    public void match(Match match) {
        throw new UnsupportedOperationException();
//        formatter.match(match);
    }

    public void result(Result result) {
        throw new UnsupportedOperationException();
//        formatter.result(result);
    }

    public void eof() {
        replayFeatureElement();
        formatter.eof();
    }

    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
        throw new UnsupportedOperationException();
//        formatter.syntaxError(state, event, legalEvents, uri, line);
    }

    public void embedding(String mimeType, byte[] data) {
        throw new UnsupportedOperationException();
    }

    private void replayFeatureElement() {
        if (featureElement != null) {
            backend.newScenario();
            formatter.steps(steps);
            featureElement.replay(formatter);
            boolean skip = false; // TODO: Add ability to instantiate entire runner with skip=false, for dry runs
            for (Step step : steps) {
                skip = execute(step, skip);
            }
            steps.clear();
        }
    }

    private boolean execute(Step step, boolean skip) {
        formatter.step(step);
        StackTraceElement stepStackTraceElement = new StackTraceElement(feature.getName() + "." + featureElement.getName(), step.getKeyword() + step.getName(), uri, step.getLine());
        StepRunner stepRunner = stepRunner(step, stepStackTraceElement);
        return stepRunner.execute(skip, formatter, stepStackTraceElement);
    }

    private StepRunner stepRunner(Step step, StackTraceElement stepStackTraceElement) {
        List<CucumberMatch> matches = stepMatches(step);
        if(matches.size() == 0) {
            return new UndefinedStepRunner(stepStackTraceElement);
        }
        if(matches.size() == 1) {
            return matches.get(0);
        } else {
            // TODO: Ambiguous for > 1
            throw new RuntimeException("TODO: Support ambiguous matches");
        }
    }

    private List<CucumberMatch> stepMatches(Step step) {
        List<CucumberMatch> result = new ArrayList<CucumberMatch>();
        for (StepDefinition stepDefinition : backend.getStepDefinitions()) {
            List<Argument> arguments = stepDefinition.argumentsFrom(step);
            if (arguments != null) {
                result.add(new CucumberMatch(arguments, stepDefinition));
            }
        }
        return result;
    }
}
