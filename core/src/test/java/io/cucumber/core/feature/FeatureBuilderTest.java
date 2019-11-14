package io.cucumber.core.feature;

import io.cucumber.core.io.Resource;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeatureBuilderTest {

    private final FeatureBuilder builder = new FeatureBuilder();

    @Test
    void ignores_identical_features_in_different_directories() throws IOException {
        URI featurePath1 = URI.create("src/example.feature");
        URI featurePath2 = URI.create("build/example.feature");

        Resource resource1 = createResourceMock(featurePath1);
        Resource resource2 = createResourceMock(featurePath2);

        builder.parse(resource1);
        builder.parse(resource2);

        List<CucumberFeature> features = builder.build();

        assertThat(features.size(), equalTo(1));
    }

    @Test
    void duplicate_content_with_different_file_names_are_intentionally_duplicated() throws IOException {
        URI featurePath1 = URI.create("src/feature1/example-first.feature");
        URI featurePath2 = URI.create("src/feature1/example-second.feature");

        Resource resource1 = createResourceMock(featurePath1);
        Resource resource2 = createResourceMock(featurePath2);

        builder.parse(resource1);
        builder.parse(resource2);

        List<CucumberFeature> features = builder.build();

        assertAll(
            () -> assertThat(features.size(), equalTo(2)),
            () -> assertThat(features.get(0).getUri(), equalTo(featurePath1)),
            () -> assertThat(features.get(1).getUri(), equalTo(featurePath2))
        );
    }


    @Test
    void features_are_sorted_by_uri() throws IOException {
        URI featurePath1 = URI.create("c.feature");
        URI featurePath2 = URI.create("b.feature");
        URI featurePath3 = URI.create("a.feature");

        Resource resource1 = createResourceMock(featurePath1);
        Resource resource2 = createResourceMock(featurePath2);
        Resource resource3 = createResourceMock(featurePath3);

        builder.parse(resource1);
        builder.parse(resource2);
        builder.parse(resource3);

        List<CucumberFeature> features = builder.build();

        assertAll(
            () -> assertThat(features.get(0).getUri(), equalTo(featurePath3)),
            () -> assertThat(features.get(1).getUri(), equalTo(featurePath2)),
            () -> assertThat(features.get(2).getUri(), equalTo(featurePath1))
        );
    }

    private Resource createResourceMock(URI featurePath) throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn(featurePath);
        ByteArrayInputStream feature = new ByteArrayInputStream("Feature: Example".getBytes(UTF_8));
        when(resource.getInputStream()).thenReturn(feature);
        return resource;
    }

}
