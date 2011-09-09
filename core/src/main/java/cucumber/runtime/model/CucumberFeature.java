package cucumber.runtime.model;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cucumber.runtime.Runtime;

public class CucumberFeature {
    private final String featureUri;
    private final Feature feature;
    private CucumberBackground background;
    private StepContainer currentStepContainer;
    private List<FeatureElement> featureElements = new ArrayList<FeatureElement>();
    private Locale locale;

    public CucumberFeature(Feature feature, String featureUri) {
        this.feature = feature;
        this.featureUri = featureUri;
    }

    public void background(Background background) {
        this.background = new CucumberBackground(this, featureUri, background);
        currentStepContainer = this.background;
    }

    public void scenario(Scenario scenario) {
        currentStepContainer = new CucumberScenario(this, featureUri, scenario);
        featureElements.add((FeatureElement)currentStepContainer);
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        currentStepContainer = new CucumberScenarioOutline(this, featureUri, scenarioOutline);
        featureElements.add((FeatureElement)currentStepContainer);
    }

    public void examples(Examples examples) {
        ((CucumberScenarioOutline)currentStepContainer).examples(examples);
    }

    public void step(Step step) {
        currentStepContainer.step(step);
    }

    public Feature getFeature() {
        return feature;
    }

    public String getFeatureUri() {
        return featureUri;
    }

    public void run(Runtime runtime, Formatter formatter, Reporter reporter) {
        formatter.uri(featureUri);
        formatter.feature(feature);
        for (FeatureElement featureElement : featureElements) {
            featureElement.run(runtime, formatter, reporter);
        }
    }

    public CucumberBackground getBackground() {
        return background;
    }

    public List<FeatureElement> getFeatureElements() {
        return featureElements;
    }

    public List<CucumberScenario> getCucumberScenarios() {
        List<CucumberScenario> scenarios = new ArrayList<CucumberScenario>();
        for (FeatureElement element : featureElements) {
            if (element instanceof CucumberScenario) {
                scenarios.add((CucumberScenario)element);
            }
        }
        return scenarios;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public Set<String> tags() {
        Set<String> tags = new HashSet<String>();
        for (Tag tag : this.getFeature().getTags()) {
            tags.add(tag.getName());
        }
        return tags;
    }
}
