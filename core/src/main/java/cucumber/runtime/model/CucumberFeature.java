package cucumber.runtime.model;

import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CucumberFeature {
    private final String uri;
    private final Feature feature;
    private CucumberBackground cucumberBackground;
    private StepContainer currentStepContainer;
    private List<CucumberFeatureElement> cucumberFeatureElements = new ArrayList<CucumberFeatureElement>();
    private Locale locale;
    private CucumberScenarioOutline currentScenarioOutline;

    public CucumberFeature(Feature feature, String uri) {
        this.feature = feature;
        this.uri = uri;
    }

    public void background(Background background) {
        cucumberBackground = new CucumberBackground(this, background);
        currentStepContainer = cucumberBackground;
    }

    public void scenario(Scenario scenario) {
        CucumberFeatureElement cucumberFeatureElement = new CucumberScenario(this, cucumberBackground, scenario);
        currentStepContainer = cucumberFeatureElement;
        cucumberFeatureElements.add(cucumberFeatureElement);
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        CucumberScenarioOutline cucumberScenarioOutline = new CucumberScenarioOutline(this, cucumberBackground, scenarioOutline);
        currentScenarioOutline = cucumberScenarioOutline;
        currentStepContainer = cucumberScenarioOutline;
        cucumberFeatureElements.add(cucumberScenarioOutline);
    }

    public void examples(Examples examples) {
        currentScenarioOutline.examples(examples);
    }

    public void step(Step step) {
        currentStepContainer.step(step);
    }

    public Feature getFeature() {
        return feature;
    }

    public List<CucumberFeatureElement> getFeatureElements() {
        return cucumberFeatureElements;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getUri() {
        return uri;
    }
}
