package io.cucumber.core.gherkin.vintage;

import gherkin.ast.ScenarioDefinition;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Scenario;

import static io.cucumber.core.gherkin.vintage.GherkinVintageLocation.from;

final class GherkinVintageScenario implements Scenario {

    private final ScenarioDefinition scenarioDefinition;

    GherkinVintageScenario(ScenarioDefinition scenarioDefinition) {
        this.scenarioDefinition = scenarioDefinition;
    }

    @Override
    public Location getLocation() {
        return from(scenarioDefinition.getLocation());
    }

    @Override
    public String getKeyWord() {
        return scenarioDefinition.getKeyword();
    }

    @Override
    public String getName() {
        return scenarioDefinition.getName();
    }
}
