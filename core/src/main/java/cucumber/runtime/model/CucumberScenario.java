package cucumber.runtime.model;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.World;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;

import java.util.List;

public class CucumberScenario extends CucumberTagStatement {
    private final CucumberBackground cucumberBackground;
    private World world;

    public CucumberScenario(CucumberFeature cucumberFeature, CucumberBackground cucumberBackground, Scenario scenario) {
        super(cucumberFeature, scenario);
        this.cucumberBackground = cucumberBackground;
    }

    public CucumberScenario(CucumberFeature cucumberFeature, CucumberBackground cucumberBackground, Scenario exampleScenario, Row example) {
        super(cucumberFeature, exampleScenario, example);
        this.cucumberBackground = cucumberBackground;
    }

    public void buildWorldAndRunBeforeHooks(List<String> gluePaths, Runtime runtime) throws Throwable {
        world = new World(runtime, tags());
        world.buildBackendWorldsAndRunBeforeHooks(gluePaths);
    }

    @Override
    public void run(Formatter formatter, Reporter reporter, Runtime runtime, List<? extends Backend> backends, List<String> gluePaths) {
        // TODO: Maybe get extraPaths from scenario

        // TODO: split up prepareAndFormat so we can run Background in isolation.
        // Or maybe just try to make Background behave like a regular Scenario?? Printing wise at least.

        try {
            buildWorldAndRunBeforeHooks(gluePaths, runtime);
        } catch (Throwable e) {
            // What do we do now???
        }

        runBackground(formatter, reporter);

        format(formatter);
        for (Step step : getSteps()) {
            runStep(step, reporter);
        }
        runAfterHooksAndDisposeWorld();
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

    public void runAfterHooksAndDisposeWorld() {
        world.runAfterHooksAndDisposeBackendWorlds();
    }

}
