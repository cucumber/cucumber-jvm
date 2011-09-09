package cucumber.runtime.model;

import gherkin.formatter.model.Background;

public class CucumberBackground extends AbstractStepContainer {
    private final Background background;

    public CucumberBackground(CucumberFeature cucumberFeature, String uri, Background background) {
        super(cucumberFeature, uri);
        this.background = background;
    }

    public Background getBackground() {
        return background;
    }
}
