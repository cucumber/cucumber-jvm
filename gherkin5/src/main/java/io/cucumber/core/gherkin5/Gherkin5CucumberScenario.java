package io.cucumber.core.gherkin5;

import gherkin.ast.ScenarioDefinition;
import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.core.gherkin.CucumberScenario;

import static io.cucumber.core.gherkin5.Gherkin5CucumberLocation.from;

final class Gherkin5CucumberScenario implements CucumberScenario {

    private final ScenarioDefinition scenarioDefinition;

    Gherkin5CucumberScenario(ScenarioDefinition scenarioDefinition) {
        this.scenarioDefinition = scenarioDefinition;
    }

    @Override
    public CucumberLocation getLocation() {
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
