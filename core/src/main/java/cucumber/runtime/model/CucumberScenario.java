package cucumber.runtime.model;

import cucumber.runtime.World;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.util.*;

public class CucumberScenario {
    private final List<Step> steps = new ArrayList<Step>();
    private final Scenario scenario;
    private final CucumberFeature cucumberFeature;
    private final String uri;

    private World world;

    public CucumberScenario(CucumberFeature cucumberFeature, String uri, Scenario scenario) {
        this.cucumberFeature = cucumberFeature;
        this.uri = uri;
        this.scenario = scenario;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void step(Step step) {
        steps.add(step);
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
}
