package io.cucumber.core.gherkin5;

import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioOutline;
import io.cucumber.core.gherkin.CucumberFeature;
import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.core.gherkin.CucumberPickle;
import io.cucumber.core.gherkin.Node;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static io.cucumber.core.gherkin5.Gherkin5CucumberLocation.from;

final class Gherkin5CucumberFeature implements CucumberFeature {
    private final URI uri;
    private final List<CucumberPickle> pickles;
    private final GherkinDocument gherkinDocument;
    private final String gherkinSource;

    Gherkin5CucumberFeature(GherkinDocument gherkinDocument, URI uri, String gherkinSource, List<CucumberPickle> pickles) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
        this.pickles = pickles;
    }

    @Override
    public Stream<Node> children() {
        return gherkinDocument.getFeature().getChildren().stream()
            .map(scenarioDefinition -> {
                if (scenarioDefinition instanceof ScenarioOutline) {
                    ScenarioOutline outline = (ScenarioOutline) scenarioDefinition;
                    return new Gherkin5CucumberScenarioOutline(outline);
                }
                return new Gherkin5CucumberScenario(scenarioDefinition);
            }).map(Node.class::cast);
    }

    @Override
    public String getKeyword() {
        return gherkinDocument.getFeature().getKeyword();
    }

    @Override
    public Optional<CucumberPickle> getPickleAt(CucumberLocation location) {
        return pickles.stream()
            .filter(cucumberPickle -> cucumberPickle.getLocation().equals(location))
            .findFirst();
    }

    @Override
    public CucumberLocation getLocation() {
        return from(gherkinDocument.getFeature().getLocation());
    }

    @Override
    public List<CucumberPickle> getPickles() {
        return pickles;
    }

    @Override
    public String getKeyWord() {
        return null;
    }

    @Override
    public String getName() {
        return gherkinDocument.getFeature().getName();
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
        Gherkin5CucumberFeature that = (Gherkin5CucumberFeature) o;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

}
