package io.cucumber.core.feature;

import gherkin.ast.ScenarioDefinition;

public final class CucumberScenario implements CucumberScenarioDefinition {

    private final ScenarioDefinition scenarioDefinition;

    CucumberScenario(ScenarioDefinition scenarioDefinition) {
        this.scenarioDefinition = scenarioDefinition;
    }

    public int getLine() {
        return scenarioDefinition.getLocation().getLine();
    }

    @Override
    public CucumberLocation getLocation() {
        return CucumberLocation.from(scenarioDefinition.getLocation());
    }

    @Override
    public String getKeyWord() {
        return scenarioDefinition.getKeyword();
    }

    public String getName() {
        return scenarioDefinition.getName();
    }
}
