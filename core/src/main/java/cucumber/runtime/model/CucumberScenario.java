package cucumber.runtime.model;

import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;

import java.util.Set;

public class CucumberScenario extends CucumberTagStatement {
    private final Scenario scenario;

    public CucumberScenario(CucumberFeature cucumberFeature, CucumberBackground cucumberBackground, Scenario scenario) {
        super(cucumberBackground, cucumberFeature, scenario);
        this.scenario = scenario;
    }

    public CucumberScenario(CucumberFeature cucumberFeature, CucumberBackground cucumberBackground, Scenario exampleScenario, Row example) {
        super(cucumberBackground, cucumberFeature, exampleScenario, example);
        this.scenario = exampleScenario;
    }

    /**
     * This method is called when Cucumber is run from the CLI, but not when run from JUnit
     */
    @Override
    public void run(Formatter formatter, Reporter reporter, Runtime runtime) {
        Set<Tag> tags = tagsAndInheritedTags();
        runtime.buildBackendWorlds(reporter, tags, scenario.getName());
        try {
            formatter.startOfScenarioLifeCycle((Scenario) getGherkinModel());
        } catch (Throwable ignore) {
            // IntelliJ has its own formatter which doesn't yet implement this.
        }
        runtime.runBeforeHooks(reporter, tags);

        runBackground(formatter, reporter, runtime);
        format(formatter);
        runSteps(reporter, runtime);

        runtime.runAfterHooks(reporter, tags);
        try {
            formatter.endOfScenarioLifeCycle((Scenario) getGherkinModel());
        } catch (Throwable ignore) {
            // IntelliJ has its own formatter which doesn't yet implement this.
        }
        runtime.disposeBackendWorlds();
    }

    private void runBackground(Formatter formatter, Reporter reporter, Runtime runtime) {
        final CucumberBackground cucumberBackground = getCucumberBackground();
        if (cucumberBackground != null) {
            cucumberBackground.format(formatter);
            cucumberBackground.runSteps(reporter, runtime);
        }
    }
}
