package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.CucumberExamples;
import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.core.gherkin.CucumberScenarioOutline;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;

import java.util.stream.Stream;

final class Gherkin8CucumberScenarioOutline implements CucumberScenarioOutline {

    private final Scenario scenario;

    Gherkin8CucumberScenarioOutline(Scenario scenario) {
        this.scenario = scenario;
    }


    @Override
    public Stream<CucumberExamples> children() {
        return scenario.getExamplesList().stream()
            .map(Gherkin8CucumberExamples::new);
    }

    @Override
    public String getKeyWord() {
        return scenario.getKeyword();
    }

    @Override
    public String getName() {
        return scenario.getName();
    }

    @Override
    public CucumberLocation getLocation() {
        return Gherkin8CucumberLocation.from(scenario.getLocation());
    }
}
