package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.CucumberExample;
import io.cucumber.core.gherkin.CucumberExamples;
import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

final class Gherkin8CucumberExamples implements CucumberExamples {

    private final Examples examples;

    Gherkin8CucumberExamples(Examples examples) {
        this.examples = examples;
    }

    @Override
    public Stream<CucumberExample> children() {
        AtomicInteger row = new AtomicInteger(1);
        return examples.getTableBodyList().stream()
            .map(tableRow -> new Gherkin8CucumberExample(tableRow, row.getAndIncrement()));
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
    public CucumberLocation getLocation() {
        return Gherkin8CucumberLocation.from(examples.getLocation());
    }
}
