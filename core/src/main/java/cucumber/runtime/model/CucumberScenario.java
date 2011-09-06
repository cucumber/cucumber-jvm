package cucumber.runtime.model;

import cucumber.runtime.Runtime;
import cucumber.runtime.World;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public void prepareAndFormat(Runtime runtime, Formatter formatter) {
        world = runtime.newWorld(tags());
        world.prepare();
        formatter.scenario(scenario);
        for (Step step : steps) {
            formatter.step(step);
        }
    }

    public void runAndDispose(Reporter reporter) {
        for (Step step : steps) {
            runStep(step, reporter);
        }
        dispose();
    }

    public void dispose() {
        world.dispose();
    }

    public void runStep(Step step, Reporter reporter) {
        world.runStep(uri, step, reporter, cucumberFeature.getLocale());
    }

    public void step(Step step) {
        steps.add(step);
    }

    private Set<String> tags() {
        Set<String> tags = new HashSet<String>();
        for (Tag tag : cucumberFeature.getFeature().getTags()) {
            tags.add(tag.getName());
        }
        for (Tag tag : scenario.getTags()) {
            tags.add(tag.getName());
        }
        return tags;
    }
}
