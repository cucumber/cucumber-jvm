package io.cucumber.core.feature;

import gherkin.ast.Feature;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Optional;

public final class CucumberFeature {
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

    public String getKeyword() {
        return gherkinDocument.getFeature().getKeyword();
    }

    public List<CucumberPickle> getPickles() {
        return pickles;
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

    public Feature getGherkinFeature() {
        return gherkinDocument.getFeature();
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

    public Optional<CucumberPickle> getPickleAt(int line) {
        return pickles.stream().filter(cucumberPickle -> cucumberPickle.getLine() == line).findFirst();
    }

}
