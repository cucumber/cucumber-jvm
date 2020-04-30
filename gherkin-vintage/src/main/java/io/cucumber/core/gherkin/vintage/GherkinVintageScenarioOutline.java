package io.cucumber.core.gherkin.vintage;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.cucumber.core.gherkin.vintage.GherkinVintageLocation.from;

final class GherkinVintageScenarioOutline implements Node.ScenarioOutline {

    private final gherkin.ast.ScenarioOutline scenarioOutline;
    private final List<Examples> children;

    GherkinVintageScenarioOutline(gherkin.ast.ScenarioOutline scenarioOutline) {
        this.scenarioOutline = scenarioOutline;
        this.children = scenarioOutline.getExamples().stream()
            .map(GherkinVintageExamples::new)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<Examples> elements() {
        return children;
    }

    @Override
    public Location getLocation() {
        return from(scenarioOutline.getLocation());
    }

    @Override
    public Optional<String> getKeyWord() {
        return Optional.of(scenarioOutline.getKeyword());
    }

    @Override
    public Optional<String> getName() {
        String name = scenarioOutline.getName();
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }
}
