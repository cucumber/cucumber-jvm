package io.cucumber.core.gherkin.vintage;

import gherkin.ast.GherkinDocument;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

final class GherkinVintageFeature implements Feature {
    private final URI uri;
    private final List<Pickle> pickles;
    private final GherkinDocument gherkinDocument;
    private final String gherkinSource;
    private final List<Node> children;

    GherkinVintageFeature(GherkinDocument gherkinDocument, URI uri, String gherkinSource, List<Pickle> pickles) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
        this.pickles = pickles;
        this.children = gherkinDocument.getFeature().getChildren().stream()
            .filter(scenarioDefinition ->
                (scenarioDefinition instanceof gherkin.ast.ScenarioOutline)
                    || (scenarioDefinition instanceof gherkin.ast.Scenario))
            .map(scenarioDefinition -> {
                if (scenarioDefinition instanceof gherkin.ast.ScenarioOutline) {
                    gherkin.ast.ScenarioOutline outline = (gherkin.ast.ScenarioOutline) scenarioDefinition;
                    return new GherkinVintageScenarioOutline(outline);
                }
                return new GherkinVintageScenario(scenarioDefinition);
            }).map(Node.class::cast)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<Node> elements() {
        return children;
    }

    @Override
    public Optional<String> getKeyWord() {
        return Optional.of(gherkinDocument.getFeature().getKeyword());
    }

    @Override
    public Pickle getPickleAt(Node node) {
        Location location = node.getLocation();
        return pickles.stream()
            .filter(pickle -> pickle.getLocation().equals(location))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No pickle in " + uri + " at " + location));
    }

    @Override
    public Location getLocation() {
        return GherkinVintageLocation.from(gherkinDocument.getFeature().getLocation());
    }

    @Override
    public List<Pickle> getPickles() {
        return pickles;
    }

    @Override
    public Optional<String> getName() {
        String name = gherkinDocument.getFeature().getName();
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public String getSource() {
        return gherkinSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GherkinVintageFeature that = (GherkinVintageFeature) o;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

}
