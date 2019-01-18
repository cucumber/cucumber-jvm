package cucumber.runtime.model;

import io.cucumber.core.io.Resource;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.model.FeatureLoader;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberFeatureTest {
    @Test
    public void succeeds_if_no_features_are_found() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.<Resource>emptyList());

        new FeatureLoader(resourceLoader).load(singletonList("does/not/exist"), new PrintStream(new ByteArrayOutputStream()));
    }

    @Test
    public void logs_message_if_no_features_are_found() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.<Resource>emptyList());

        new FeatureLoader(resourceLoader).load(singletonList("does/not/exist"), new PrintStream(baos));

        assertEquals(String.format("No features found at [does/not/exist]%n"), baos.toString());
    }

    @Test
    public void logs_message_if_no_feature_paths_are_given() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);

        new FeatureLoader(resourceLoader).load(Collections.<String>emptyList(), new PrintStream(baos));

        assertEquals(String.format("Got no path to feature directory or feature file%n"), baos.toString());
    }

    @Test
    public void gives_error_message_if_path_does_not_exist() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        mockFeaturePathToNotExist(resourceLoader, "path/bar.feature");
        try {
            new FeatureLoader(resourceLoader).load(singletonList("path/bar.feature"), new PrintStream(new ByteArrayOutputStream()));
            fail("IllegalArgumentException was expected");
        } catch (IllegalArgumentException exception) {
            assertEquals("Not a file or directory: path/bar.feature", exception.getMessage());
        }
    }
    @Test
    public void gives_error_message_if_feature_on_class_path_does_not_exist() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        mockFeaturePathToNotExist(resourceLoader, "classpath:path/bar.feature");
        try {
            new FeatureLoader(resourceLoader).load(singletonList("classpath:path/bar.feature"), new PrintStream(new ByteArrayOutputStream()));
            fail("IllegalArgumentException was expected");
        } catch (IllegalArgumentException exception) {
            assertEquals("Feature not found: classpath:path/bar.feature", exception.getMessage());
        }
    }

    private void mockFeaturePathToNotExist(ResourceLoader resourceLoader, String featurePath) {
        if (featurePath.startsWith("classpath")) {
            when(resourceLoader.resources(featurePath, ".feature")).thenReturn(new ArrayList<>());
        } else {
            when(resourceLoader.resources(featurePath, ".feature")).thenThrow(new IllegalArgumentException("Not a file or directory: " + featurePath));
        }
    }

    private String suffix(String suffix) {
        return suffix;
    }
}
