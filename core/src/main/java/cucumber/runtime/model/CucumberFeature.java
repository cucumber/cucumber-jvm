package cucumber.runtime.model;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.FeatureBuilder;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CucumberFeature {
    private final String uri;
    private final Feature feature;
    private CucumberBackground cucumberBackground;
    private StepContainer currentStepContainer;
    private List<CucumberTagStatement> cucumberTagStatements = new ArrayList<CucumberTagStatement>();
    private Locale locale;
    private CucumberScenarioOutline currentScenarioOutline;

    // TODO: Use an iterable here, wrapping around
    public static List<CucumberFeature> load(ResourceLoader resourceLoader, List<String> featurePaths, final List<Object> filters) {
        final List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
        for (String featurePath : featurePaths) {
            Iterable<Resource> resources = resourceLoader.resources(featurePath, ".feature");
            for (Resource resource : resources) {
                builder.parse(resource, filters);
            }
        }
        return cucumberFeatures;
    }


    public CucumberFeature(Feature feature, String uri) {
        this.feature = feature;
        this.uri = uri;
    }

    public void background(Background background) {
        cucumberBackground = new CucumberBackground(this, background);
        currentStepContainer = cucumberBackground;
    }

    public void scenario(Scenario scenario) {
        CucumberTagStatement cucumberTagStatement = new CucumberScenario(this, cucumberBackground, scenario);
        currentStepContainer = cucumberTagStatement;
        cucumberTagStatements.add(cucumberTagStatement);
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        CucumberScenarioOutline cucumberScenarioOutline = new CucumberScenarioOutline(this, cucumberBackground, scenarioOutline);
        currentScenarioOutline = cucumberScenarioOutline;
        currentStepContainer = cucumberScenarioOutline;
        cucumberTagStatements.add(cucumberScenarioOutline);
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

    public List<CucumberTagStatement> getFeatureElements() {
        return cucumberTagStatements;
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
