package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

final class GherkinMessagesFeature implements Feature {

    private final io.cucumber.messages.types.Feature feature;
    private final URI uri;
    private final List<Pickle> pickles;
    private final List<Envelope> envelopes;
    private final String gherkinSource;
    private final List<Node> children;

    GherkinMessagesFeature(
            io.cucumber.messages.types.Feature feature,
            URI uri,
            String gherkinSource,
            List<Pickle> pickles,
            List<Envelope> envelopes
    ) {
        this.feature = requireNonNull(feature);
        this.uri = requireNonNull(uri);
        this.gherkinSource = requireNonNull(gherkinSource);
        this.pickles = requireNonNull(pickles);
        this.envelopes = requireNonNull(envelopes);
        this.children = feature.getChildren().stream()
                .filter(this::hasRuleOrScenario)
                .map(this::mapRuleOrScenario)
                .collect(Collectors.toList());
    }

    private Node mapRuleOrScenario(FeatureChild featureChild) {
        if (featureChild.getRule().isPresent()) {
            return new GherkinMessagesRule(this, featureChild.getRule().get());
        }

        io.cucumber.messages.types.Scenario scenario = featureChild.getScenario().get();
        if (!scenario.getExamples().isEmpty()) {
            return new GherkinMessagesScenarioOutline(this, scenario);
        }
        return new GherkinMessagesScenario(this, scenario);
    }

    private boolean hasRuleOrScenario(FeatureChild featureChild) {
        return featureChild.getRule().isPresent() || featureChild.getScenario().isPresent();
    }

    @Override
    public Collection<Node> elements() {
        return children;
    }

    @Override
    public Location getLocation() {
        return GherkinMessagesLocation.from(feature.getLocation());
    }

    @Override
    public Optional<String> getKeyword() {
        return Optional.of(feature.getKeyword());
    }

    @Override
    public Optional<String> getName() {
        String name = feature.getName();
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.empty();
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GherkinMessagesFeature that = (GherkinMessagesFeature) o;
        return uri.equals(that.uri);
    }

}
