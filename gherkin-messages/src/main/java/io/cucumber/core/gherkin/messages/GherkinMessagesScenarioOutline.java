package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.ScenarioOutline;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;

import java.util.stream.Stream;

final class GherkinMessagesScenarioOutline implements ScenarioOutline {

    private final Scenario scenario;

    GherkinMessagesScenarioOutline(Scenario scenario) {
        this.scenario = scenario;
    }


    @Override
    public Stream<Examples> children() {
        return scenario.getExamplesList().stream()
            .map(GherkinMessagesExamples::new);
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
        return GherkinMessagesLocation.from(scenario.getLocation());
    }
}
