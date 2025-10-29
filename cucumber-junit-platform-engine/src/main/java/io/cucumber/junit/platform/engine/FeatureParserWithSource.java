package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.resource.Resource;

import java.util.Optional;

class FeatureParserWithSource {

    private final FeatureParser delegate;

    FeatureParserWithSource(FeatureParser delegate) {
        this.delegate = delegate;
    }

    Optional<FeatureWithSource> parseResource(Resource resource) {
        return delegate.parseResource(resource).map(feature -> {
            FeatureSource featureSource = FeatureSource.of(resource.getUri());
            return new FeatureWithSource(feature, featureSource);
        });
    }
}
