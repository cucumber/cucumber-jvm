package io.cucumber.core.gherkin.messages;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Optional;

final class GherkinMessagesScenario implements Node.Scenario {

    private final io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario scenario;

    GherkinMessagesScenario(io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public Location getLocation() {
        return GherkinMessagesLocation.from(scenario.getLocation());
    }

    @Override
    public Optional<String> getKeyword() {
        return Optional.of(scenario.getKeyword());
    }

    @Override
    public Optional<String> getName() {
        String name = scenario.getName();
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

}
