package cucumber.runtime.model;

import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;

import java.util.Set;

public class CucumberScenario extends CucumberTagStatement {
    private final CucumberBackground cucumberBackground;
    private final Scenario scenario;

    public CucumberScenario(CucumberFeature cucumberFeature, CucumberBackground cucumberBackground, Scenario scenario) {
        super(cucumberFeature, scenario);
        this.cucumberBackground = cucumberBackground;
        this.scenario = scenario;
    }

    public CucumberScenario(CucumberFeature cucumberFeature, CucumberBackground cucumberBackground, Scenario exampleScenario, Row example) {
        super(cucumberFeature, exampleScenario, example);
        this.cucumberBackground = cucumberBackground;
        this.scenario = exampleScenario;
    }

    public CucumberBackground getCucumberBackground() {
        return cucumberBackground;
    }

    /**
     * This method is called when Cucumber is run from the CLI or JUnit
     */
    @Override
    public void run(Formatter formatter, Reporter reporter, Runtime runtime) {
        Set<Tag> tags = tagsAndInheritedTags();
        runtime.buildBackendWorlds(reporter, tags, scenario);
        formatter.startOfScenarioLifeCycle((Scenario) getGherkinModel());
        runtime.runBeforeHooks(reporter, tags);

        runBackground(formatter, reporter, runtime);
        format(formatter);
        runSteps(reporter, runtime);

        runtime.runAfterHooks(reporter, tags);
        formatter.endOfScenarioLifeCycle((Scenario) getGherkinModel());
        runtime.disposeBackendWorlds(createScenarioDesignation());
    }

    private String createScenarioDesignation() {
        return cucumberFeature.getPath() + ":" + Integer.toString(scenario.getLine()) + " # " +
                scenario.getKeyword() + ": " + scenario.getName();
    }

    private void runBackground(Formatter formatter, Reporter reporter, Runtime runtime) {
        if (cucumberBackground != null) {
            cucumberBackground.format(formatter);
            cucumberBackground.runSteps(reporter, runtime);
        }
    }
}
