package cucumber.runtime.model;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

import cucumber.runtime.Runtime;
import cucumber.runtime.World;

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

    public void disposeWorld() {   
        world.runAfterHooks();
        world.dispose();
    }

    public void newWorld(Runtime runtime) {
        world = runtime.newWorld();
        world.runBeforeHooks();
    }

    public void run(Runtime runtime, Formatter formatter, Reporter reporter) {
        newWorld(runtime);
        formatter.scenario(scenario);        
        for (Step step : steps) {
            formatter.step(step);
        }
        for (Step step : steps) {
            runStep(step, reporter);
        }        
        disposeWorld();
    }

    public void runStep(Step step, Reporter reporter) {
        world.runStep(uri, step, reporter, cucumberFeature.getLocale());
    }

//    private Reporter makeReporter(Step step, RunNotifier notifier) {
//        Description description = describeChild(step);
//        final EachTestNotifier eachTestNotifier = new EachTestNotifier(notifier, description);
//        return new JUnitReporter(eachTestNotifier);
//    }

    public void step(Step step) {
        steps.add(step);
    }
}
