package io.cucumber.core.gherkin;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

public interface CucumberFeature {
    String getKeyword();

    List<CucumberPickle> getPickles();

    String getName();

    URI getUri();

    String getSource();

    class CucumberFeatureUriComparator implements Comparator<CucumberFeature> {
        @Override
        public int compare(CucumberFeature a, CucumberFeature b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
