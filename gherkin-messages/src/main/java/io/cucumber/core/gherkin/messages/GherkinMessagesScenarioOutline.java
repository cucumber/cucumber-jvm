package io.cucumber.core.gherkin.messages;

import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

final class GherkinMessagesScenarioOutline implements Node.ScenarioOutline {

    private final Messages.GherkinDocument.Feature.Scenario scenario;
    private final List<Examples> children;

    GherkinMessagesScenarioOutline(Messages.GherkinDocument.Feature.Scenario scenario) {
        this.scenario = scenario;
        this.children = scenario.getExamplesList().stream()
                .map(GherkinMessagesExamples::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Examples> elements() {
        return children;
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
