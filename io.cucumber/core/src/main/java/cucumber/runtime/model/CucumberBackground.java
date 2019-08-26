package cucumber.runtime.model;

import gherkin.formatter.model.Background;

public class CucumberBackground extends StepContainer {
    public CucumberBackground(CucumberFeature cucumberFeature, Background background) {
        super(cucumberFeature, background);
    }
}
