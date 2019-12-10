package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

final class Gherkin8Examples implements Examples {

    private final io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples examples;

    Gherkin8Examples(io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples examples) {
        this.examples = examples;
    }

    @Override
    public Stream<Example> children() {
        AtomicInteger row = new AtomicInteger(1);
        return examples.getTableBodyList().stream()
            .map(tableRow -> new Gherkin8Example(tableRow, row.getAndIncrement()));
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
        return Gherkin8Location.from(examples.getLocation());
    }
}
