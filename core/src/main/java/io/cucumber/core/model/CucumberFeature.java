package io.cucumber.core.model;

import gherkin.ast.GherkinDocument;
import gherkin.events.PickleEvent;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class CucumberFeature{
    private final URI uri;
    private final List<PickleEvent> pickles;
    private GherkinDocument gherkinDocument;
    private String gherkinSource;


    public CucumberFeature(GherkinDocument gherkinDocument, URI uri, String gherkinSource, List<PickleEvent> pickles) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
        this.pickles = pickles;
    }

    public List<PickleEvent> getPickles() {
        return pickles;
    }

    public String getName() {
        return gherkinDocument.getFeature().getName();
    }

    public GherkinDocument getGherkinFeature() {
        return gherkinDocument;
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
