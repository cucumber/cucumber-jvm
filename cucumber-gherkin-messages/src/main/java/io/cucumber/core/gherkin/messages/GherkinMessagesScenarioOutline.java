package io.cucumber.core.gherkin.messages;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

final class GherkinMessagesScenarioOutline implements Node.ScenarioOutline {

    private final io.cucumber.messages.types.Scenario scenario;
    private final List<Examples> children;
    private final Node parent;

    GherkinMessagesScenarioOutline(Node parent, io.cucumber.messages.types.Scenario scenario) {
        this.parent = parent;
        this.scenario = scenario;
        AtomicInteger examplesIndex = new AtomicInteger(1);
        this.children = scenario.getExamples().stream()
                .map(examples -> new GherkinMessagesExamples(this, examples, examplesIndex.getAndIncrement()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.of(parent);
    }

    @Override
    public Collection<Examples> elements() {
        return children;
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
