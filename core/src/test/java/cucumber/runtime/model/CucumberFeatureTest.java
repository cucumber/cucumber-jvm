package cucumber.runtime.model;

import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberFeatureTest {

    private static final String DOES_NOT_EXIST = "does/not/exist";

    @Test
    public void succeeds_if_no_features_are_found() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        mockNonExistingResource(resourceLoader);
        new FeatureLoader(resourceLoader).load(feature("does/not/exist"));
    }

    private void mockNonExistingResource(ResourceLoader resourceLoader) {
        when(resourceLoader.resources(URI.create(DOES_NOT_EXIST), ".feature")).thenReturn(Collections.<Resource>emptyList());
    }


    @Test
    public void gives_error_message_if_path_does_not_exist() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        mockFeaturePathToNotExist(resourceLoader, "path/bar.feature");
        try {
            new FeatureLoader(resourceLoader).load(feature("path/bar.feature"));
            fail("IllegalArgumentException was expected");
        } catch (IllegalArgumentException exception) {
            assertEquals("Not a file or directory: path/bar.feature", exception.getMessage());
        }
    }

    public List<URI> feature(String s) {
        return singletonList(URI.create(s));
    }

    @Test
    public void gives_error_message_if_feature_on_class_path_does_not_exist() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        mockFeaturePathToNotExist(resourceLoader, "classpath:path/bar.feature");
        try {
            new FeatureLoader(resourceLoader).load(feature("classpath:path/bar.feature"));
            fail("IllegalArgumentException was expected");
        } catch (IllegalArgumentException exception) {
            assertEquals("Feature not found: classpath:path/bar.feature", exception.getMessage());
        }
    }

    private void mockFeaturePathToNotExist(ResourceLoader resourceLoader, String featurePath) {
        if (featurePath.startsWith("classpath")) {
            when(resourceLoader.resources(URI.create(featurePath), ".feature")).thenReturn(new ArrayList<Resource>());
        } else {
            when(resourceLoader.resources(URI.create(featurePath), ".feature")).thenThrow(new IllegalArgumentException("Not a file or directory: " + featurePath));
        }
    }

    private String suffix(String suffix) {
        return suffix;
    }
}
