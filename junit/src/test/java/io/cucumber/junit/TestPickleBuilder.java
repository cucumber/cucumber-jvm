package io.cucumber.junit;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

class TestPickleBuilder {

    private TestPickleBuilder() {
    }

    static List<Pickle> picklesFromFeature(final String path, final String source) {
        return parseFeature(path, source).getPickles();
    }

    static Feature parseFeature(final String path, final String source) {
        return parseFeature(URI.create(path), source);
    }

    private static Feature parseFeature(final URI path, final String source) {
        return new FeatureParser(UUID::randomUUID).parseResource(new Resource() {
            @Override
            public URI getUri() {
                return path;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
            }

        }).orElse(null);
    }

}
