package io.cucumber.core.gherkin5;

import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioOutline;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Located;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Node;
import io.cucumber.messages.Messages;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static io.cucumber.core.gherkin5.Gherkin5Location.from;
import static java.util.Collections.emptyList;

final class Gherkin5Feature implements Feature {
    private final URI uri;
    private final List<Pickle> pickles;
    private final GherkinDocument gherkinDocument;
    private final String gherkinSource;

    Gherkin5Feature(GherkinDocument gherkinDocument, URI uri, String gherkinSource, List<Pickle> pickles) {
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
                    return new Gherkin5ScenarioOutline(outline);
                }
                return new Gherkin5Scenario(scenarioDefinition);
            }).map(Node.class::cast);
    }

    @Override
    public String getKeyword() {
        return gherkinDocument.getFeature().getKeyword();
    }

    @Override
    public Optional<Pickle> getPickleAt(Located located) {
        Location location = located.getLocation();
        return pickles.stream()
            .filter(cucumberPickle -> cucumberPickle.getLocation().equals(location))
            .findFirst();
    }

    @Override
    public Location getLocation() {
        return from(gherkinDocument.getFeature().getLocation());
    }

    @Override
    public List<Pickle> getPickles() {
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
    public Iterable<Messages.Envelope> getMessages() {
        return emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gherkin5Feature that = (Gherkin5Feature) o;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

}
