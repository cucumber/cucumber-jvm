package io.cucumber.core.gherkin.messages;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

final class GherkinMessagesExamples implements Node.Examples {

    private final io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples examples;
    private final List<Example> children;

    GherkinMessagesExamples(io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples examples) {
        this.examples = examples;

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
    public String getKeyWord() {
        return examples.getKeyword();
    }

    @Override
    public String getName() {
        return examples.getName();
    }

    @Override
    public Location getLocation() {
        return GherkinMessagesLocation.from(examples.getLocation());
    }
}
