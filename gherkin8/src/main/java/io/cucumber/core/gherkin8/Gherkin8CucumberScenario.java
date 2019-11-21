package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.core.gherkin.CucumberScenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;

final class Gherkin8CucumberScenario implements CucumberScenario {

    private final Scenario scenario;

    Gherkin8CucumberScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public String getKeyWord() {
        return scenario.getKeyword();
    }

    @Override
    public String getName() {
        return scenario.getName();
    }

    @Override
    public CucumberLocation getLocation() {
        return Gherkin8CucumberLocation.from(scenario.getLocation());
    }
}
