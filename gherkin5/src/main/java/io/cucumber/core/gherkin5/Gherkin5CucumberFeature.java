package io.cucumber.core.gherkin5;

import gherkin.ast.GherkinDocument;
import io.cucumber.core.gherkin.CucumberFeature;
import io.cucumber.core.gherkin.CucumberPickle;

import java.net.URI;
import java.util.List;
import java.util.Objects;

public final class Gherkin5CucumberFeature implements CucumberFeature {
    private final URI uri;
    private final List<CucumberPickle> pickles;
    private final GherkinDocument gherkinDocument;
    private final String gherkinSource;

    Gherkin5CucumberFeature(GherkinDocument gherkinDocument, URI uri, String gherkinSource, List<CucumberPickle> pickles) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
        this.pickles = pickles;
    }

    @Override
    public String getKeyword() {
        return gherkinDocument.getFeature().getKeyword();
    }

    @Override
    public List<CucumberPickle> getPickles() {
        return pickles;
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
        Gherkin5CucumberFeature that = (Gherkin5CucumberFeature) o;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

}
