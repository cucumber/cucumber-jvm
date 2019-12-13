package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

final class GherkinMessagesExamples implements Examples {

    private final io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples examples;

    GherkinMessagesExamples(io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples examples) {
        this.examples = examples;
    }

    @Override
    public Stream<Example> children() {
        AtomicInteger row = new AtomicInteger(1);
        return examples.getTableBodyList().stream()
            .map(tableRow -> new GherkinMessagesExample(tableRow, row.getAndIncrement()));
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
