package cucumber.runtime.model;

import gherkin.formatter.model.Step;

import java.util.List;

public interface StepContainer {
    public List<Step> getSteps();
    public void step(Step step);
}
