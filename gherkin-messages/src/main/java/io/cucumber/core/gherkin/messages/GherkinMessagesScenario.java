package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Scenario;

final class GherkinMessagesScenario implements Scenario {

    private final io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario scenario;

    GherkinMessagesScenario(io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario scenario) {
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
        return GherkinMessagesLocation.from(scenario.getLocation());
    }
}
