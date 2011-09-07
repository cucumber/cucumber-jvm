package cucumber.runtime.model;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.util.List;
import java.util.Set;

import cucumber.runtime.World;

public class CucumberScenario extends AbstractFeatureElement {
    private final Scenario scenario;

    public CucumberScenario(CucumberFeature cucumberFeature, String uri, Scenario scenario) {
        super(cucumberFeature, uri);
        this.scenario = scenario;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void run(World world, Formatter formatter, Reporter reporter, List<Step> stepsToRun) {
        formatter.scenario(scenario);
        for (Step step : stepsToRun) {
            formatter.step(step);
        }
        for (Step step : stepsToRun) {
            world.runStep(getUri(), step, reporter, getCucumberFeature().getLocale());
        }
    }

    public void runStep(Step step, Reporter reporter) {
        getWorld().runStep(getUri(), step, reporter, getCucumberFeature().getLocale());
    }

    public Set<String> tags() {
        Set<String> tags = getCucumberFeature().tags();
        for (Tag tag : scenario.getTags()) {
            tags.add(tag.getName());
        }
        return tags;
    }
}
