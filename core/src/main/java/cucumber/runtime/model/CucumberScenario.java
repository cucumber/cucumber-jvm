package cucumber.runtime.model;

import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeWorld;
import cucumber.runtime.World;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Scenario;

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
        world = new RuntimeWorld(runtime, tags());
        return world;
    }

    /**
     * This method is called when Cucumber is run from the CLI, but not when run from JUnit
     */
    @Override
    public void run(Formatter formatter, Reporter reporter, Runtime runtime) {
        World world = newWorld(runtime);
        world.buildBackendWorldsAndRunBeforeHooks(reporter);
        runBackground(formatter, reporter);
        formatAndRunSteps(formatter, reporter, world);
        world.runAfterHooksAndDisposeBackendWorlds(reporter);
    }

    public void runBackground(Formatter formatter, Reporter reporter) {
        if (cucumberBackground != null) {
            cucumberBackground.formatAndRunSteps(formatter, reporter, world);
        }
    }
}
