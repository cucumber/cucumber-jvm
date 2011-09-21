package cucumber.runtime.model;

import gherkin.formatter.model.Background;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CucumberFeature {
    private final String uri;
    private final Feature feature;
    private CucumberBackground cucumberBackground;
    private StepContainer currentStepContainer;
    private List<CucumberScenario> cucumberScenarios = new ArrayList<CucumberScenario>();
    private Locale locale;

    public CucumberFeature(Feature feature, String uri) {
        this.feature = feature;
        this.uri = uri;
    }

    public void background(Background background) {
        cucumberBackground = new CucumberBackground(this, background);
        currentStepContainer = cucumberBackground;
    }

    public void scenario(Scenario scenario) {
        CucumberScenario cucumberScenario = new CucumberScenario(this, uri, cucumberBackground, scenario);
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

    public String getUri() {
        return uri;
    }
}
