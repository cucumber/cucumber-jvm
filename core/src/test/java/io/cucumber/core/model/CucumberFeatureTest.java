package io.cucumber.core.model;

import io.cucumber.core.io.ResourceLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Collections;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberFeatureTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void succeeds_if_no_features_are_found() {
        URI featurePath = URI.create("does/not/exist");
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(featurePath, ".feature")).thenReturn(Collections.emptyList());
        new FeatureLoader(resourceLoader).load(singletonList(featurePath));
    }

    @Test
    public void gives_error_message_if_path_does_not_exist() {
        URI featurePath = URI.create("path/bar.feature");
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(featurePath, ".feature")).thenThrow(new IllegalArgumentException("Not a file or directory: " + "path/bar.feature"));
        expectedException.expectMessage("Not a file or directory: path/bar.feature");
        new FeatureLoader(resourceLoader).load(singletonList(featurePath));
    }

    @Test
    public void gives_error_message_if_feature_on_class_path_does_not_exist() {
        URI featurePath = URI.create("classpath:path/bar.feature");
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(featurePath, ".feature")).thenReturn(emptyList());
        expectedException.expectMessage("Feature not found: classpath:path/bar.feature");
        new FeatureLoader(resourceLoader).load(singletonList(featurePath));
    }
}
