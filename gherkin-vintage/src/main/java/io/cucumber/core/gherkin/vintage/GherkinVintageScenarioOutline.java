package io.cucumber.core.gherkin.vintage;

import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.ScenarioOutline;

import java.util.stream.Stream;

import static io.cucumber.core.gherkin.vintage.GherkinVintageLocation.from;

final class GherkinVintageScenarioOutline implements ScenarioOutline {

    private final gherkin.ast.ScenarioOutline scenarioOutline;

    GherkinVintageScenarioOutline(gherkin.ast.ScenarioOutline scenarioOutline) {
        this.scenarioOutline = scenarioOutline;
    }

    @Override
    public Stream<Examples> children() {
        return scenarioOutline.getExamples().stream()
            .map(GherkinVintageExamples::new);
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
