package io.cucumber.core.gherkin.legacy;

import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.ScenarioOutline;

import java.util.stream.Stream;

import static io.cucumber.core.gherkin.legacy.GherkinLegacyLocation.from;

final class GherkinLegacyScenarioOutline implements ScenarioOutline {

    private final gherkin.ast.ScenarioOutline scenarioOutline;

    GherkinLegacyScenarioOutline(gherkin.ast.ScenarioOutline scenarioOutline) {
        this.scenarioOutline = scenarioOutline;
    }

    @Override
    public Stream<Examples> children() {
        return scenarioOutline.getExamples().stream()
            .map(GherkinLegacyExamples::new);
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
