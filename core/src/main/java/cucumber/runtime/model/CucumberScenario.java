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

    public World newWorld(Runtime runtime) {
        world = new World(runtime, tags());
        return world;
    }

    /**
     * This method is called when Cucumber is run from the CLI, but not when run from JUnit
     */
    @Override
    public void run(Formatter formatter, Reporter reporter, Runtime runtime, List<? extends Backend> backends, List<String> gluePaths) {
        // TODO: Maybe get extraPaths from scenario
        // TODO: maybe just try to make Background behave like a regular Scenario?? Printing wise at least.

        World world = newWorld(runtime);
        world.buildBackendWorldsAndRunBeforeHooks(gluePaths, reporter);

        try {
            runBackground(formatter, reporter);
        } catch (Throwable t) {
            // TODO What do we do now???
            t.printStackTrace();
        }

        try {
            formatAndRunSteps(formatter, reporter, world);
        } catch (Throwable t) {
            // TODO What do we do now???
            t.printStackTrace();
        }

        world.runAfterHooksAndDisposeBackendWorlds(reporter);
    }

    public void runBackground(Formatter formatter, Reporter reporter) throws Throwable {
        if (cucumberBackground != null) {
            cucumberBackground.formatAndRunSteps(formatter, reporter, world);
        }
    }
}
