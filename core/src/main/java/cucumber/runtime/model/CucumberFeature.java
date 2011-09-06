package cucumber.runtime.model;

import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
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

    public void run(Runtime runtime, Formatter formatter, Reporter reporter) {
        formatter.uri(featureUri);
        formatter.feature(feature);
        for (CucumberScenario cucumberScenario : cucumberScenarios) {
            cucumberScenario.prepareAndFormat(runtime, formatter);
            cucumberScenario.runAndDispose(reporter);
        }
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
