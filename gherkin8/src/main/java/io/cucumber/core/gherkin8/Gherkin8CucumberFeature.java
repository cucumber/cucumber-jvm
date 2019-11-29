package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.CucumberFeature;
import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.core.gherkin.CucumberPickle;
import io.cucumber.core.gherkin.Node;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class Gherkin8CucumberFeature implements CucumberFeature {
    private final URI uri;
    private final List<CucumberPickle> pickles;
    private final List<Messages.Envelope> envelopes;
    private final GherkinDocument gherkinDocument;
    private final String gherkinSource;

    Gherkin8CucumberFeature(GherkinDocument gherkinDocument, URI uri, String gherkinSource, List<CucumberPickle> pickles, List<Messages.Envelope> envelopes) {
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
                    return new Gherkin8CucumberRule(featureChild.getRule());
                }

                Scenario scenario = featureChild.getScenario();
                if (scenario.getExamplesCount() > 0) {
                    return new Gherkin8CucumberScenarioOutline(scenario);
                } else {
                    return new Gherkin8CucumberScenario(scenario);
                }
            });
    }

    @Override
    public String getKeyword() {
        return gherkinDocument.getFeature().getKeyword();
    }

    @Override
    public CucumberLocation getLocation() {
        return Gherkin8CucumberLocation.from(gherkinDocument.getFeature().getLocation());
    }

    @Override
    public Optional<CucumberPickle> getPickleAt(CucumberLocation location) {
        return pickles.stream()
            .filter(cucumberPickle -> cucumberPickle.getLocation().equals(location))
            .findFirst();
    }

    @Override
    public List<CucumberPickle> getPickles() {
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
    public Iterable<Messages.Envelope> getMessages() {
        return envelopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gherkin8CucumberFeature that = (Gherkin8CucumberFeature) o;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    public GherkinDocument getGherkinDocument() {
        return gherkinDocument;
    }
}
