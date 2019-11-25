package io.cucumber.core.feature;

import gherkin.ast.ScenarioOutline;

import java.util.stream.Stream;

public final class CucumberScenarioOutline implements CucumberScenarioDefinition, Container<CucumberExamples> {

    private final ScenarioOutline scenarioOutline;

    CucumberScenarioOutline(ScenarioOutline scenarioOutline) {
        this.scenarioOutline = scenarioOutline;
    }

    @Override
    public Stream<CucumberExamples> children() {
        return scenarioOutline.getExamples().stream()
            .map(CucumberExamples::new);
    }

    @Override
    public CucumberLocation getLocation() {
        return CucumberLocation.from(scenarioOutline.getLocation());
    }

    @Override
    public String getKeyWord() {
        return scenarioOutline.getKeyword();
    }

    public String getName() {
        return scenarioOutline.getName();
    }
}
