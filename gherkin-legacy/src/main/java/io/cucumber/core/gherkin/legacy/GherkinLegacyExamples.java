package io.cucumber.core.gherkin.legacy;

import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.cucumber.core.gherkin.legacy.GherkinLegacyLocation.from;

public final class GherkinLegacyExamples implements Examples {

    private final gherkin.ast.Examples examples;

    GherkinLegacyExamples(gherkin.ast.Examples examples) {
        this.examples = examples;
    }

    @Override
    public Stream<Example> children() {
        if (examples.getTableBody() == null) {
            return Stream.empty();
        }

        AtomicInteger rowCounter = new AtomicInteger(1);
        return examples.getTableBody().stream()
            .map(tableRow -> new GherkinLegacyExample(tableRow, rowCounter.getAndIncrement()));
    }

    @Override
    public Location getLocation() {
        return GherkinLegacyLocation.from(examples.getLocation());
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
