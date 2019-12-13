package io.cucumber.core.gherkin.vintage;

import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

final class GherkinVintageExamples implements Examples {

    private final gherkin.ast.Examples examples;

    GherkinVintageExamples(gherkin.ast.Examples examples) {
        this.examples = examples;
    }

    @Override
    public Stream<Example> children() {
        if (examples.getTableBody() == null) {
            return Stream.empty();
        }

        AtomicInteger rowCounter = new AtomicInteger(1);
        return examples.getTableBody().stream()
            .map(tableRow -> new GherkinVintageExample(tableRow, rowCounter.getAndIncrement()));
    }

    @Override
    public Location getLocation() {
        return GherkinVintageLocation.from(examples.getLocation());
    }

    @Override
    public String getKeyWord() {
        return examples.getKeyword();
    }

    @Override
    public String getName() {
        return examples.getName();
    }
}
