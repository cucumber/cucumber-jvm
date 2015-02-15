package cucumber.runtime;

import gherkin.formatter.model.Background;

@Deprecated
public class CucumberBackground extends StepContainer {
    public CucumberBackground(CucumberFeature cucumberFeature, Background background) {
        super(cucumberFeature, background);
    }
}
