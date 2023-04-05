package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.Resource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class CachingFeatureParser {

    private final Map<URI, Optional<Feature>> cache = new HashMap<>();
    private final FeatureParser delegate;

    CachingFeatureParser(FeatureParser delegate) {
        this.delegate = delegate;
    }

    Optional<Feature> parseResource(Resource resource) {
        return cache.computeIfAbsent(resource.getUri(), uri -> delegate.parseResource(resource));
    }
}
