package io.cucumber.core.gherkin.messages;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

final class GherkinMessagesExamples implements Node.Examples {

    private final io.cucumber.messages.types.Examples examples;
    private final List<Example> children;
    private final Location location;
    private final Node parent;

    GherkinMessagesExamples(Node parent, io.cucumber.messages.types.Examples examples, int examplesIndex) {
        this.parent = parent;
        this.examples = examples;
        this.location = GherkinMessagesLocation.from(examples.getLocation());
        AtomicInteger row = new AtomicInteger(1);
        this.children = examples.getTableBody().stream()
                .map(tableRow -> new GherkinMessagesExample(this, tableRow, examplesIndex, row.getAndIncrement()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Example> elements() {
        return children;
    }

    @Override
    public URI getUri() {
        return parent.getUri();
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

    @Override
    public Optional<Node> getParent() {
        return Optional.of(parent);
    }

}
