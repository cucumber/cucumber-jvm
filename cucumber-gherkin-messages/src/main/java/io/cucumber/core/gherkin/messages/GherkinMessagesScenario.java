package io.cucumber.core.gherkin.messages;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.net.URI;
import java.util.Optional;

final class GherkinMessagesScenario implements Node.Scenario {

    private final Node parent;
    private final io.cucumber.messages.types.Scenario scenario;

    GherkinMessagesScenario(Node parent, io.cucumber.messages.types.Scenario scenario) {
        this.parent = parent;
        this.scenario = scenario;
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.of(parent);
    }

    @Override
    public URI getUri() {
        return parent.getUri();
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
