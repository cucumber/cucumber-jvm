package cucumber.runtime.model;

import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CucumberScenario extends StepContainer {
    private final CucumberFeature cucumberFeature;
    private final String uri;
    private final CucumberBackground cucumberBackground;
    private final Scenario scenario;

    public CucumberScenario(CucumberFeature cucumberFeature, String uri, CucumberBackground cucumberBackground, Scenario scenario) {
        super(scenario);
        this.cucumberFeature = cucumberFeature;
        this.uri = uri;
        this.cucumberBackground = cucumberBackground;
        this.scenario = scenario;
    }

    public Set<String> tags() {
        Set<String> tags = new HashSet<String>();
        for (Tag tag : cucumberFeature.getFeature().getTags()) {
            tags.add(tag.getName());
        }
        for (Tag tag : scenario.getTags()) {
            tags.add(tag.getName());
        }
        return tags;
    }

    public String getUri() {
        return uri;
    }

    public Locale getLocale() {
        return cucumberFeature.getLocale();
    }

    public String getName() {
        return scenario.getKeyword() + ": " + scenario.getName();
    }

    public CucumberBackground getCucumberBackground() {
        return cucumberBackground;
    }
}
