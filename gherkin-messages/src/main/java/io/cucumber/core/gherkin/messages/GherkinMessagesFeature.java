package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
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

    private final GherkinDocument.Feature feature;
    private final URI uri;
    private final List<Pickle> pickles;
    private final List<Messages.Envelope> envelopes;
    private final String gherkinSource;
    private final List<Node> children;

    GherkinMessagesFeature(
            GherkinDocument.Feature feature,
            URI uri,
            String gherkinSource,
            List<Pickle> pickles,
            List<Messages.Envelope> envelopes
    ) {
        this.feature = requireNonNull(feature);
        this.uri = requireNonNull(uri);
        this.gherkinSource = requireNonNull(gherkinSource);
        this.pickles = requireNonNull(pickles);
        this.envelopes = requireNonNull(envelopes);
        this.children = feature.getChildrenList().stream()
                .filter(this::hasRuleOrScenario)
                .map(this::mapRuleOrScenario)
                .collect(Collectors.toList());
    }

    private Node mapRuleOrScenario(FeatureChild featureChild) {
        if (featureChild.hasRule()) {
            return new GherkinMessagesRule(featureChild.getRule());
        }

        GherkinDocument.Feature.Scenario scenario = featureChild.getScenario();
        if (scenario.getExamplesCount() > 0) {
            return new GherkinMessagesScenarioOutline(scenario);
        }
        return new GherkinMessagesScenario(scenario);
    }

    private boolean hasRuleOrScenario(FeatureChild featureChild) {
        return featureChild.hasRule() || featureChild.hasScenario();
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
