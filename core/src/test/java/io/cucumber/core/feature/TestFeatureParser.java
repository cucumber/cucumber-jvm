package io.cucumber.core.feature;

import io.cucumber.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class TestFeatureParser {
    public static CucumberFeature parse(final String source) {
        return parse("file:test.feature", source);
    }

    public static CucumberFeature parse(final String uri, final String source) {
        return parse(FeatureIdentifier.parse(uri), source);
    }

    public static CucumberFeature parse(final URI uri, final String source) {
        return FeatureParser.parseResource(new Resource() {
            @Override
            public URI getPath() {
                return uri;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
            }

        });
    }
}
