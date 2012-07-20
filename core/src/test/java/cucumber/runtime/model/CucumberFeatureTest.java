package cucumber.runtime.model;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.CucumberException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberFeatureTest {
    @Test
    public void fails_if_no_features_are_found() {
        try {
            ResourceLoader resourceLoader = mock(ResourceLoader.class);
            when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.<Resource>emptyList());
            CucumberFeature.load(resourceLoader, asList("does/not/exist"), emptyList());
            fail("Should have failed");
        } catch (CucumberException e) {
            assertEquals("No features found at [does/not/exist]", e.getMessage());
        }
    }

    @Test
    public void fails_if_features_are_found_but_filters_are_too_strict() throws IOException {
        try {
            ResourceLoader resourceLoader = mock(ResourceLoader.class);

            Resource resource = mock(Resource.class);
            when(resource.getPath()).thenReturn("foo.feature");
            when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("Feature: foo".getBytes("UTF-8")));

            when(resourceLoader.resources("features", ".feature")).thenReturn(asList(resource));
            CucumberFeature.load(resourceLoader, asList("features"), asList((Object) "@nowhere"));
            fail("Should have failed");
        } catch (CucumberException e) {
            assertEquals("None of the features at [features] matched the filters: [@nowhere]", e.getMessage());
        }
    }
}
