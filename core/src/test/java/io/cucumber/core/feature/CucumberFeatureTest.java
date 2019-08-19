package io.cucumber.core.feature;

import io.cucumber.core.io.ResourceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.net.URI;
import java.util.Collections;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberFeatureTest {

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
        Executable testMethod = () -> new FeatureLoader(resourceLoader).load(singletonList(featurePath));
        IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Not a file or directory: path/bar.feature"
        )));
    }

    @Test
    public void gives_error_message_if_feature_on_class_path_does_not_exist() {
        URI featurePath = URI.create("classpath:path/bar.feature");
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(featurePath, ".feature")).thenReturn(emptyList());
        Executable testMethod = () -> new FeatureLoader(resourceLoader).load(singletonList(featurePath));
        IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Feature not found: classpath:path/bar.feature"
        )));

    }

}
