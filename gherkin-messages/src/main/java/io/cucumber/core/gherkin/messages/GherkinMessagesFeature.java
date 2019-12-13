package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Located;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Node;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

final class GherkinMessagesFeature implements Feature {
    private final URI uri;
    private final List<Pickle> pickles;
    private final List<Messages.Envelope> envelopes;
    private final GherkinDocument gherkinDocument;
    private final String gherkinSource;

    GherkinMessagesFeature(GherkinDocument gherkinDocument, URI uri, String gherkinSource, List<Pickle> pickles, List<Messages.Envelope> envelopes) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
        this.pickles = pickles;
        this.envelopes = envelopes;
    }

    @Override
    public Stream<Node> children() {
        return gherkinDocument.getFeature().getChildrenList().stream()
            .filter(featureChild -> featureChild.hasRule() || featureChild.hasScenario())
            .map(featureChild -> {
                if (featureChild.hasRule()) {
                    return new GherkinMessagesRule(featureChild.getRule());
                }

                Scenario scenario = featureChild.getScenario();
                if (scenario.getExamplesCount() > 0) {
                    return new GherkinMessagesScenarioOutline(scenario);
                } else {
                    return new GherkinMessagesScenario(scenario);
                }
            });
    }

    @Override
    public String getKeyword() {
        return gherkinDocument.getFeature().getKeyword();
    }

    @Override
    public Location getLocation() {
        return GherkinMessagesLocation.from(gherkinDocument.getFeature().getLocation());
    }

    @Override
    public Optional<Pickle> getPickleAt(Located located) {
        Location location = located.getLocation();
        return pickles.stream()
            .filter(cucumberPickle -> cucumberPickle.getLocation().equals(location))
            .findFirst();
    }

    @Override
    public List<Pickle> getPickles() {
        return pickles;
    }

    @Override
    public String getKeyWord() {
        return gherkinDocument.getFeature().getKeyword();
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
        GherkinMessagesFeature that = (GherkinMessagesFeature) o;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

}
