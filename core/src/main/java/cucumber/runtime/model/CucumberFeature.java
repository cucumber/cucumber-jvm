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
    private Background background;
    private CucumberScenario currentCucumberScenario;
    private List<CucumberScenario> cucumberScenarios = new ArrayList<CucumberScenario>();
    private Locale locale;

    public CucumberFeature(Feature feature, String featureUri) {
        this.feature = feature;
        this.featureUri = featureUri;
    }

    public void background(Background background) {
        this.background = background;
    }

    public void scenario(Scenario scenario) {
        currentCucumberScenario = new CucumberScenario(this, featureUri, scenario);
        cucumberScenarios.add(currentCucumberScenario);
    }

    public void step(Step step) {
        currentCucumberScenario.step(step);
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
