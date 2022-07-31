package io.cucumber.core.runtime;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.Resource;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier.FeatureBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

class FeatureBuilderTest {

    private final FeatureParser parser = new FeatureParser(UUID::randomUUID);
    private final FeatureBuilder builder = new FeatureBuilder();

    @Test
    void ignores_identical_features_in_different_directories() {
        URI featurePath1 = URI.create("src/example.feature");
        URI featurePath2 = URI.create("build/example.feature");

        Feature resource1 = createResourceMock(featurePath1);
        Feature resource2 = createResourceMock(featurePath2);

        builder.addUnique(resource1);
        builder.addUnique(resource2);

        List<Feature> features = builder.build();

        assertThat(features.size(), equalTo(1));
    }

    private Feature createResourceMock(URI featurePath) {
        return parser.parseResource(new Resource() {
            @Override
            public URI getUri() {
                return featurePath;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream("Feature: Example\n  Scenario: Empty".getBytes(UTF_8));
            }
        }).orElse(null);
    }

    @Test
    void duplicate_content_with_different_file_names_are_intentionally_duplicated() {
        URI featurePath1 = URI.create("src/feature1/example-first.feature");
        URI featurePath2 = URI.create("src/feature1/example-second.feature");

        Feature resource1 = createResourceMock(featurePath1);
        Feature resource2 = createResourceMock(featurePath2);

        builder.addUnique(resource1);
        builder.addUnique(resource2);

        List<Feature> features = builder.build();

        assertAll(
            () -> assertThat(features.size(), equalTo(2)),
            () -> assertThat(features.get(0).getUri(), equalTo(featurePath1)),
            () -> assertThat(features.get(1).getUri(), equalTo(featurePath2)));
    }

    @Test
    void features_are_sorted_by_uri() {
        URI featurePath1 = URI.create("c.feature");
        URI featurePath2 = URI.create("b.feature");
        URI featurePath3 = URI.create("a.feature");

        Feature resource1 = createResourceMock(featurePath1);
        Feature resource2 = createResourceMock(featurePath2);
        Feature resource3 = createResourceMock(featurePath3);

        builder.addUnique(resource1);
        builder.addUnique(resource2);
        builder.addUnique(resource3);

        List<Feature> features = builder.build();

        assertAll(
            () -> assertThat(features.get(0).getUri(), equalTo(featurePath3)),
            () -> assertThat(features.get(1).getUri(), equalTo(featurePath2)),
            () -> assertThat(features.get(2).getUri(), equalTo(featurePath1)));
    }

}
