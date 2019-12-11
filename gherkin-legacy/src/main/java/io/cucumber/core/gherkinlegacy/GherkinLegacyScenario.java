package io.cucumber.core.gherkinlegacy;

import gherkin.ast.ScenarioDefinition;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Scenario;

import static io.cucumber.core.gherkinlegacy.GherkinLegacyLocation.from;

final class GherkinLegacyScenario implements Scenario {

    private final ScenarioDefinition scenarioDefinition;

    GherkinLegacyScenario(ScenarioDefinition scenarioDefinition) {
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
