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

    public CucumberScenario(CucumberFeature cucumberFeature, CucumberBackground cucumberBackground, Scenario scenario) {
        super(cucumberFeature, scenario);
        this.cucumberBackground = cucumberBackground;
    }

    public CucumberScenario(CucumberFeature cucumberFeature, CucumberBackground cucumberBackground, Scenario exampleScenario, Row example) {
        super(cucumberFeature, exampleScenario, example);
        this.cucumberBackground = cucumberBackground;
    }

    /**
     * This method is called when Cucumber is run from the CLI, but not when run from JUnit
     */
    @Override
    public void run(Formatter formatter, Reporter reporter, World world) {
        world.buildBackendContextAndRunBeforeHooks(reporter, tags());
        runBackground(formatter, reporter, world);
        formatAndRunSteps(formatter, reporter, world);
        world.runAfterHooksAndDisposeBackendContext(reporter, tags());
    }

    public void runBackground(Formatter formatter, Reporter reporter, World world) {
        if (cucumberBackground != null) {
            cucumberBackground.formatAndRunSteps(formatter, reporter, world);
        }
    }
}
