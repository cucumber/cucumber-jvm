package io.cucumber.core.gherkin.messages;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

final class GherkinMessagesExamples implements Node.Examples {

    private final io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples examples;
    private final List<Example> children;
    private final Location location;

    GherkinMessagesExamples(io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples examples) {
        this.examples = examples;
        this.location = GherkinMessagesLocation.from(examples.getLocation());
        AtomicInteger row = new AtomicInteger(1);
        this.children = examples.getTableBodyList().stream()
                .map(tableRow -> new GherkinMessagesExample(tableRow, row.getAndIncrement()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Example> elements() {
        return children;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Optional<String> getKeyword() {
        return Optional.of(examples.getKeyword());
    }

    @Override
    public Optional<String> getName() {
        String name = examples.getName();
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

}
