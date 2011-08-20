package cucumber.runtime.model;

import cucumber.runtime.*;
import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CucumberScenario {
    private final List<Step> steps = new ArrayList<Step>();
    private final Feature feature;
    private final Background background;
    private final Scenario scenario;
    private final String uri;

    private World world;
    
    public CucumberScenario(String uri, Feature feature, Background background, Scenario scenario) {
        this.uri = uri;
        this.feature = feature;
        this.background = background;
        this.scenario = scenario;
    }

    public Scenario getScenario() {
        return scenario;
    }

    protected List<Step> getSteps() {
        return steps;
    }

    public void run(Runtime runtime, Formatter formatter, Reporter reporter, Locale locale) {
        world = runtime.newWorld();
        formatter.scenario(scenario);
        for (Step step : steps) {
            formatter.step(step);
        }
        for (Step step : steps) {
            runStep(step, reporter, locale);
        }
        world.dispose();
    }

    protected void runStep(Step step, Reporter reporter, Locale locale) {
//        Reporter reporter = makeReporter(step, notifier);
        world.runStep(step, uri + ":" + step.getLine(), reporter, locale);
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
