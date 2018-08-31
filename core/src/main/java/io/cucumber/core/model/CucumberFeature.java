package io.cucumber.core.model;

import gherkin.ast.GherkinDocument;

import java.io.Serializable;
import java.util.Comparator;

public final class CucumberFeature{
    private final String uri;
    private final GherkinDocument gherkinDocument;
    private final String gherkinSource;

    public CucumberFeature(GherkinDocument gherkinDocument, String uri, String gherkinSource) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
    }

    public GherkinDocument getGherkinFeature() {
        return gherkinDocument;
    }

    public String getUri() {
        return uri;
    }

    public String getGherkinSource() {
        return gherkinSource;
    }

    public static class CucumberFeatureUriComparator implements Comparator<CucumberFeature> {
        @Override
        public int compare(CucumberFeature a, CucumberFeature b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
