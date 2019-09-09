package io.cucumber.core.feature;

import gherkin.ast.GherkinDocument;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    public static class CucumberFeatureUriComparator implements Comparator<CucumberFeature> {
        @Override
        public int compare(CucumberFeature a, CucumberFeature b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
