package io.cucumber.core.gherkin.vintage;

import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.ScenarioOutline;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static io.cucumber.core.gherkin.vintage.GherkinVintageLocation.from;

final class GherkinVintageScenarioOutline implements ScenarioOutline {

    private final gherkin.ast.ScenarioOutline scenarioOutline;
    private final List<Examples> children;

    GherkinVintageScenarioOutline(gherkin.ast.ScenarioOutline scenarioOutline) {
        this.scenarioOutline = scenarioOutline;
        this.children = scenarioOutline.getExamples().stream()
            .map(GherkinVintageExamples::new)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<Examples> children() {
        return children;
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
