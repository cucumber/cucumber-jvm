package io.cucumber.core.gherkin.vintage;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Optional;

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
    public Optional<String> getKeyWord() {
        return Optional.of(scenarioDefinition.getKeyword());
    }

    @Override
    public Optional<String> getName() {
        String name = scenarioDefinition.getName();
        return name.isEmpty() ? Optional.empty() :Optional.of(name);
    }
}
