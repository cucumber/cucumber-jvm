package io.cucumber.junit.platform.engine;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class FeatureParserWithCaching {

    private final Map<URI, Optional<Feature>> cache = new HashMap<>();
    private final FeatureParserWithIssueReporting delegate;

    FeatureParserWithCaching(FeatureParserWithIssueReporting delegate) {
        this.delegate = delegate;
    }

    Optional<Feature> parseResource(Resource resource) {
        return cache.computeIfAbsent(resource.getUri(), uri -> delegate.parseResource(resource));
    }

    Optional<Feature> parseResource(Path resource) {
        return parseResource(new PathAdapter(resource));
    }

    Optional<Feature> parseResource(org.junit.platform.commons.support.Resource resource) {
        return parseResource(new ResourceAdapter(resource));
    }

    private static class ResourceAdapter implements Resource {
        private final org.junit.platform.commons.support.Resource resource;

        public ResourceAdapter(org.junit.platform.commons.support.Resource resource) {
            this.resource = resource;
        }

        @Override
        public URI getUri() {
            String name = resource.getName();
            try {
                return new URI("classpath", name, null);
            } catch (URISyntaxException e) {
                String message = String.format("Could not create classpath uri for resource '%s'", name);
                throw new CucumberException(message, e);
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return resource.getInputStream();
        }
    }

    private static class PathAdapter implements Resource {
        private final Path resource;

        public PathAdapter(Path resource) {
            this.resource = resource;
        }

        @Override
        public URI getUri() {
            return resource.toUri();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(resource);
        }
    }

}
