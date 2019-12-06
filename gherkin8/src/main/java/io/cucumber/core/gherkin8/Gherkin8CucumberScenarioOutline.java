package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.CucumberScenarioOutline;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;

import java.util.stream.Stream;

final class Gherkin8CucumberScenarioOutline implements CucumberScenarioOutline {

    private final Scenario scenario;

    Gherkin8CucumberScenarioOutline(Scenario scenario) {
        this.scenario = scenario;
    }


    @Override
    public Stream<Examples> children() {
        return scenario.getExamplesList().stream()
            .map(Gherkin8Examples::new);
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
    public Location getLocation() {
        return Gherkin8Location.from(scenario.getLocation());
    }
}
