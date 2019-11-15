package io.cucumber.junit;

import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

class TestFeatureParser {
    static CucumberFeature parse(final String source) {
        return parse("file:test.feature", source);
    }

    private static CucumberFeature parse(final String uri, final String source) {
        return parse(FeatureIdentifier.parse(uri), source);
    }

    private static CucumberFeature parse(final URI uri, final String source) {
        return FeatureParser.parseResource(new Resource() {
            @Override
            public URI getUri() {
                return uri;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
            }

        });
    }
}
