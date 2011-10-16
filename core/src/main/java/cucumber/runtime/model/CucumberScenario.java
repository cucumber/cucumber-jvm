package cucumber.runtime.model;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.World;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

public class CucumberScenario extends CucumberTagStatement {
    private final CucumberBackground cucumberBackground;
    private World world;

    public CucumberScenario(CucumberFeature cucumberFeature, CucumberBackground cucumberBackground, Scenario scenario) {
        super(cucumberFeature, scenario);
        this.cucumberBackground = cucumberBackground;
    }

    public void createWorld(List<String> codePaths, Runtime runtime) {
//        List<String> allCodePaths = new ArrayList<String>(codePaths);
//        allCodePaths.addAll(extraCodePaths);

        world = new World(runtime, tags());
        world.prepare(codePaths);
    }

    @Override
    public void run(Formatter formatter, Reporter reporter, Runtime runtime, List<Backend> backends) {
        // TODO: Maybe get extraPaths from scenario

        // TODO: split up prepareAndFormat so we can run Background in isolation.
        // Or maybe just try to make Background behave like a regular Scenario?? Printing wise at least.

        createWorld(new ArrayList<String>(), runtime);

        runBackground(formatter, reporter);

        format(formatter);
        for (Step step : getSteps()) {
            runStep(step, reporter);
        }
        disposeWorld();
    }

    public Throwable runBackground(Formatter formatter, Reporter reporter) {
        if (cucumberBackground == null) {
            return null;
        }
        cucumberBackground.format(formatter);
        List<Step> steps = cucumberBackground.getSteps();
        Throwable failure = null;
        for (Step step : steps) {
            Throwable e = runStep(step, reporter);
            if (e != null) {
                failure = e;
            }
        }
        return failure;
    }

    public Throwable runStep(Step step, Reporter reporter) {
        return world.runStep(getUri(), step, reporter, getLocale());
    }

    public void disposeWorld() {
        world.dispose();
    }

}
