package io.cucumber.core.gherkin.vintage;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

final class GherkinVintageExamples implements Node.Examples {

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
    public Collection<Example> elements() {
        return children;
    }

    @Override
    public Location getLocation() {
        return GherkinVintageLocation.from(examples.getLocation());
    }

    @Override
    public Optional<String> getKeyWord() {
        return Optional.of(examples.getKeyword());
    }

    @Override
    public Optional<String> getName() {
        return examples.getName().isEmpty() ? Optional.empty() : Optional.of(examples.getName());
    }
}
