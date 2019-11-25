package io.cucumber.core.feature;

import gherkin.ast.Examples;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class CucumberExamples implements Located, Named, Container<CucumberExample> {

    private final Examples examples;

    CucumberExamples(Examples examples) {
        this.examples = examples;
    }

    @Override
    public Stream<CucumberExample> children() {
        if (examples.getTableBody() == null) {
            return Stream.empty();
        }

        AtomicInteger rowCounter = new AtomicInteger(1);
        return examples.getTableBody().stream()
            .map(tableRow -> new CucumberExample(tableRow, rowCounter.getAndIncrement()));
    }

    @Override
    public CucumberLocation getLocation() {
        return CucumberLocation.from(examples.getLocation());
    }

    @Override
    public String getKeyWord() {
        return examples.getKeyword();
    }

    public String getName() {
        return examples.getName();
    }
}
