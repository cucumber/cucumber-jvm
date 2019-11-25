package io.cucumber.junit;

import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

class TestPickleBuilder {

    private TestPickleBuilder() {
    }

    static List<CucumberPickle> picklesFromFeature(final String path, final String source) {
        return parseFeature(path, source).getPickles();
    }

    static CucumberFeature parseFeature(final String path, final String source) {
        return parseFeature(URI.create(path), source);
    }

    private static CucumberFeature parseFeature(final URI path, final String source) {
        return FeatureParser.parseResource(new Resource() {
            @Override
            public URI getUri() {
                return path;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
            }

        });
    }
}
