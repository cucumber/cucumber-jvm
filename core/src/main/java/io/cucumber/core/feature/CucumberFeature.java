package io.cucumber.core.feature;

import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioOutline;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class CucumberFeature implements Located, Named, Container<CucumberScenarioDefinition> {
    private final URI uri;
    private final List<CucumberPickle> pickles;
    private final GherkinDocument gherkinDocument;
    private final String gherkinSource;

    CucumberFeature(GherkinDocument gherkinDocument, URI uri, String gherkinSource, List<CucumberPickle> pickles) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
        this.pickles = pickles;
    }

    @Override
    public Stream<CucumberScenarioDefinition> children() {
        return gherkinDocument.getFeature().getChildren()
            .stream()
            .map(scenarioDefinition -> {
                if (scenarioDefinition instanceof ScenarioOutline) {
                    ScenarioOutline scenarioOutline = (ScenarioOutline) scenarioDefinition;
                    return new CucumberScenarioOutline(scenarioOutline);
                }
                return new CucumberScenario(scenarioDefinition);
            });
    }

    public String getKeyword() {
        return gherkinDocument.getFeature().getKeyword();
    }

    @Override
    public CucumberLocation getLocation() {
        return CucumberLocation.from(gherkinDocument.getFeature().getLocation());
    }

    public List<CucumberPickle> getPickles() {
        return pickles;
    }

    @Override
    public String getKeyWord() {
        return gherkinDocument.getFeature().getKeyword();
    }

    public String getName() {
        return gherkinDocument.getFeature().getName();
    }

    public URI getUri() {
        return uri;
    }

    public String getSource() {
        return gherkinSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CucumberFeature that = (CucumberFeature) o;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    public Optional<CucumberPickle> getPickleAt(CucumberLocation line) {
        return pickles.stream().filter(cucumberPickle -> cucumberPickle.getLocation().equals(line)).findFirst();
    }

}
