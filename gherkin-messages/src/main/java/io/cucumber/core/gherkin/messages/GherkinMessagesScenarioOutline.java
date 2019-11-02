package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.ScenarioOutline;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

final class GherkinMessagesScenarioOutline implements ScenarioOutline {

    private final Scenario scenario;
    private final List<Examples> children;

    GherkinMessagesScenarioOutline(Scenario scenario) {
        this.scenario = scenario;
        this.children = scenario.getExamplesList().stream()
            .map(GherkinMessagesExamples::new)
            .collect(Collectors.toList());
    }


    @Override
    public Collection<Examples> children() {
        return children;
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
