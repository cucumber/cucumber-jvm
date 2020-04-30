package io.cucumber.core.gherkin.vintage;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

final class GherkinVintageScenario implements Node.Scenario {

    private final gherkin.ast.ScenarioDefinition scenarioDefinition;

    GherkinVintageScenario(gherkin.ast.ScenarioDefinition scenarioDefinition) {
        this.scenarioDefinition = scenarioDefinition;
    }

    @Override
    public Location getLocation() {
        return GherkinVintageLocation.from(scenarioDefinition.getLocation());
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
