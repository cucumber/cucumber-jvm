package io.cucumber.core.gherkin5;

import gherkin.ast.Examples;
import io.cucumber.core.gherkin.CucumberExample;
import io.cucumber.core.gherkin.CucumberExamples;
import io.cucumber.core.gherkin.CucumberLocation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.cucumber.core.gherkin5.Gherkin5CucumberLocation.from;

public final class Gherkin5CucumberExamples implements CucumberExamples {

    private final Examples examples;

    Gherkin5CucumberExamples(Examples examples) {
        this.examples = examples;
    }

    @Override
    public Stream<CucumberExample> children() {
        if (examples.getTableBody() == null) {
            return Stream.empty();
        }

        AtomicInteger rowCounter = new AtomicInteger(1);
        return examples.getTableBody().stream()
            .map(tableRow -> new Gherkin5CucumberExample(tableRow, rowCounter.getAndIncrement()));
    }

    @Override
    public CucumberLocation getLocation() {
        return from(examples.getLocation());
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
