package cucumber.runtime.model;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.World;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Scenario;

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

    public World buildWorldAndRunBeforeHooks(List<String> gluePaths, Runtime runtime) throws Throwable {
        world = new World(runtime, tags());
        world.buildBackendWorldsAndRunBeforeHooks(gluePaths);
        // TODO: If before hooks fail, we can't return the world, but we need it to run (skipped) steps
        return world;
    }

    /**
     * This method is called when Cucumber is run from the CLI, but not when run from JUnit
     */
    @Override
    public void run(Formatter formatter, Reporter reporter, Runtime runtime, List<? extends Backend> backends, List<String> gluePaths) {
        // TODO: Maybe get extraPaths from scenario

        // TODO: split up prepareAndFormat so we can run Background in isolation.
        // Or maybe just try to make Background behave like a regular Scenario?? Printing wise at least.

        try {
            buildWorldAndRunBeforeHooks(gluePaths, runtime);
        } catch (Throwable e) {
            // TODO What do we do now??? #106
        }

        try {
            runBackground(formatter, reporter);
        } catch (Throwable t) {
            // TODO What do we do now??? #106
        }

        try {
            formatAndRunSteps(formatter, reporter, world);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        try {
            runAfterHooksAndDisposeWorld();
        } catch (Throwable t) {
            // TODO What do we do now??? #106
        }
    }

    public void runBackground(Formatter formatter, Reporter reporter) throws Throwable {
        if (cucumberBackground != null) {
            cucumberBackground.formatAndRunSteps(formatter, reporter, world);
        }
    }

    public void runAfterHooksAndDisposeWorld() throws Throwable {
        world.runAfterHooksAndDisposeBackendWorlds();
    }

}
