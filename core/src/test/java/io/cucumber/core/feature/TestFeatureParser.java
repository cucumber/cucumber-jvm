package io.cucumber.core.feature;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestFeatureParser {

    public static Feature parse(final String source) {
        return parse("file:test.feature", source);
    }

    public static Feature parse(final String uri, final String source) {
        return parse(FeatureIdentifier.parse(uri), source);
    }

    public static Feature parse(final URI uri, final String source) {
        return parse(uri, new ByteArrayInputStream(source.getBytes(UTF_8)));
    }

    public static Feature parse(final String uri, final InputStream source) {
        return parse(FeatureIdentifier.parse(uri), source);
    }

    public static Feature parse(final URI uri, final InputStream source) {
        return new FeatureParser(UUID::randomUUID).parseResource(new Resource() {
            @Override
            public URI getUri() {
                return uri;
            }

            @Override
            public InputStream getInputStream() {
                return source;
            }

        }).orElse(null);
    }

}
