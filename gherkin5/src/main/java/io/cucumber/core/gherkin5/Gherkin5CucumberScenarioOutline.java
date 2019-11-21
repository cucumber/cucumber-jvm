package io.cucumber.core.gherkin5;

import gherkin.ast.ScenarioOutline;
import io.cucumber.core.gherkin.CucumberExamples;
import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.core.gherkin.CucumberScenarioOutline;

import java.util.stream.Stream;

import static io.cucumber.core.gherkin5.Gherkin5CucumberLocation.from;

public final class Gherkin5CucumberScenarioOutline implements CucumberScenarioOutline {

    private final ScenarioOutline scenarioOutline;

    Gherkin5CucumberScenarioOutline(ScenarioOutline scenarioOutline) {
        this.scenarioOutline = scenarioOutline;
    }

    @Override
    public Stream<CucumberExamples> children() {
        return scenarioOutline.getExamples().stream()
            .map(Gherkin5CucumberExamples::new);
    }

    @Override
    public CucumberLocation getLocation() {
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
