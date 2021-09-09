package io.cucumber.java8;

import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

class TestFeatureParser {

    static Feature parse(final String source) {
        return parse("file:test.feature", source);
    }

    private static Feature parse(final String uri, final String source) {
        return parse(FeatureIdentifier.parse(uri), source);
    }

    private static Feature parse(final URI uri, final String source) {
        return new FeatureParser(UUID::randomUUID).parseResource(new Resource() {
            @Override
            public URI getUri() {
                return uri;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
            }

        }).orElse(null);
    }

}
