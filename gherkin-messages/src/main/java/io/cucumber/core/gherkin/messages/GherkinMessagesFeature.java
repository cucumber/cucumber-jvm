package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

final class GherkinMessagesFeature implements Feature {

    private final URI uri;
    private final List<Pickle> pickles;
    private final List<Messages.Envelope> envelopes;
    private final GherkinDocument gherkinDocument;
    private final String gherkinSource;
    private final List<Node> children;

    GherkinMessagesFeature(GherkinDocument gherkinDocument, URI uri, String gherkinSource, List<Pickle> pickles, List<Messages.Envelope> envelopes) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
        this.pickles = pickles;
        this.envelopes = envelopes;
        this.children = gherkinDocument.getFeature().getChildrenList().stream()
            .filter(featureChild -> featureChild.hasRule() || featureChild.hasScenario())
            .map(featureChild -> {
                if (featureChild.hasRule()) {
                    return new GherkinMessagesRule(featureChild.getRule());
                }
                GherkinDocument.Feature.Scenario scenario = featureChild.getScenario();
                if (scenario.getExamplesCount() > 0) {
                    return new GherkinMessagesScenarioOutline(scenario);
                } else {
                    return new GherkinMessagesScenario(scenario);
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    public Collection<Node> elements() {
        return children;
    }

    @Override
    public Location getLocation() {
        return GherkinMessagesLocation.from(gherkinDocument.getFeature().getLocation());
    }

    @Override
    public Optional<String> getKeyword() {
        return Optional.of(gherkinDocument.getFeature().getKeyword());
    }

    @Override
    public Optional<String> getName() {
        String name = gherkinDocument.getFeature().getName();
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
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
    public List<Pickle> getPickles() {
        return pickles;
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
    public Iterable<?> getParseEvents() {
        return envelopes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GherkinMessagesFeature that = (GherkinMessagesFeature) o;
        return uri.equals(that.uri);
    }

}
