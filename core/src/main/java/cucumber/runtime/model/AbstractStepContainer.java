package cucumber.runtime.model;

import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStepContainer implements StepContainer {
    private final List<Step> steps = new ArrayList<Step>();
    private final CucumberFeature cucumberFeature;
    private final String uri;

    public AbstractStepContainer(CucumberFeature cucumberFeature, String uri) {
        this.cucumberFeature = cucumberFeature;
        this.uri = uri;
    }

    public CucumberFeature getCucumberFeature() {
        return cucumberFeature;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public List<Step> getSteps() {
        return steps;
    }

    @Override
    public void step(Step step) {
        steps.add(step);
    }
}
