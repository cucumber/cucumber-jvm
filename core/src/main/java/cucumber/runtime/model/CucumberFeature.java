package cucumber.runtime.model;

import gherkin.formatter.model.Background;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CucumberFeature {
    private final String featureUri;
    private final Feature feature;
    private CucumberBackground cucumberBackground;
    private StepContainer currentStepContainer;
    private List<CucumberScenario> cucumberScenarios = new ArrayList<CucumberScenario>();
    private Locale locale;

    public CucumberFeature(Feature feature, String featureUri) {
        this.feature = feature;
        this.featureUri = featureUri;
    }

    public void background(Background background) {
        cucumberBackground = new CucumberBackground(background);
        currentStepContainer = cucumberBackground;
    }

    public void scenario(Scenario scenario) {
        CucumberScenario cucumberScenario = new CucumberScenario(this, featureUri, cucumberBackground, scenario);
        currentStepContainer = cucumberScenario;
        cucumberScenarios.add(cucumberScenario);
    }

    public void step(Step step) {
        currentStepContainer.step(step);
    }

    public Feature getFeature() {
        return feature;
    }

    public List<CucumberScenario> getCucumberScenarios() {
        return cucumberScenarios;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }
}
