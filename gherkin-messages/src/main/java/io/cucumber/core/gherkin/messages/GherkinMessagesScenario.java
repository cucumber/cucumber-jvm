package io.cucumber.core.gherkin.messages;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

final class GherkinMessagesScenario implements Node.Scenario {

    private final io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario scenario;

    GherkinMessagesScenario(io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public String getKeyword() {
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
