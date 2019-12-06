package io.cucumber.core.gherkin5;

import gherkin.ast.ScenarioOutline;
import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.CucumberScenarioOutline;

import java.util.stream.Stream;

import static io.cucumber.core.gherkin5.Gherkin5Location.from;

final class Gherkin5CucumberScenarioOutline implements CucumberScenarioOutline {

    private final ScenarioOutline scenarioOutline;

    Gherkin5CucumberScenarioOutline(ScenarioOutline scenarioOutline) {
        this.scenarioOutline = scenarioOutline;
    }

    @Override
    public Stream<Examples> children() {
        return scenarioOutline.getExamples().stream()
            .map(Gherkin5Examples::new);
    }

    @Override
    public Location getLocation() {
        return from(scenarioOutline.getLocation());
    }

    @Override
    public String getKeyWord() {
        return scenarioOutline.getKeyword();
    }

    @Override
    public String getName() {
        return scenarioOutline.getName();
    }
}
