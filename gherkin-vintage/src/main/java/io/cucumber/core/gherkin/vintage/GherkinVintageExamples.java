package io.cucumber.core.gherkin.vintage;

import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

final class GherkinVintageExamples implements Examples {

    private final List<Example> children;
    private final gherkin.ast.Examples examples;

    GherkinVintageExamples(gherkin.ast.Examples examples) {
        this.examples = examples;
        if (examples.getTableBody() == null) {
            this.children = Collections.emptyList();
        } else {
            AtomicInteger rowCounter = new AtomicInteger(1);
            this.children = examples.getTableBody().stream()
                .map(tableRow -> new GherkinVintageExample(tableRow, rowCounter.getAndIncrement()))
                .collect(Collectors.toList());
        }
    }

    @Override
    public Collection<Example> children() {
        return children;
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
