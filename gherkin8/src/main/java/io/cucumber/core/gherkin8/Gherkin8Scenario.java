package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Scenario;

final class Gherkin8Scenario implements Scenario {

    private final io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario scenario;

    Gherkin8Scenario(io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario scenario) {
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
    public Location getLocation() {
        return Gherkin8Location.from(scenario.getLocation());
    }
}
