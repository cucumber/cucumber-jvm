package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Feature;

import static java.util.Objects.requireNonNull;

final class FeatureWithSource {

    private final Feature feature;
    private final FeatureSource source;

    FeatureWithSource(Feature feature, FeatureSource source) {
        this.feature = requireNonNull(feature);
        this.source = requireNonNull(source);
    }

    Feature getFeature() {
        return feature;
    }

    FeatureSource getSource() {
        return source;
    }

    String getUri() {
        return feature.getUri().toString();
    }
}
