package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.Resource;

import java.io.IOException;
import java.io.InputStream;
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
    Optional<Feature> parseResource(org.junit.platform.commons.support.Resource resource) {
        return cache.computeIfAbsent(resource.getUri(), uri -> delegate.parseResource(new ResourceAdapter(resource)));
    }

    private static class ResourceAdapter implements Resource {
        private final org.junit.platform.commons.support.Resource resource;

        public ResourceAdapter(org.junit.platform.commons.support.Resource resource) {
            this.resource = resource;
        }

        @Override
        public URI getUri() {
            return resource.getUri();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return resource.getInputStream();
        }
    }
}
