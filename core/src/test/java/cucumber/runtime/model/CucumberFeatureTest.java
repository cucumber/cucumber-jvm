package cucumber.runtime.model;

import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberFeatureTest {
    @Test
    public void succeds_if_no_features_are_found() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.<Resource>emptyList());

        CucumberFeature.load(resourceLoader, asList("does/not/exist"), emptyList(), null);
    }

    @Test
    public void logs_message_if_no_features_are_found() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.<Resource>emptyList());

        CucumberFeature.load(resourceLoader, asList("does/not/exist"), emptyList(), new PrintStream(baos));

        assertEquals(String.format("No features found at [does/not/exist]%n"), baos.toString());
    }

    @Test
    public void logs_message_if_features_are_found_but_filters_are_too_strict() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn("foo.feature");
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("Feature: foo".getBytes("UTF-8")));
        when(resourceLoader.resources("features", ".feature")).thenReturn(asList(resource));

        CucumberFeature.load(resourceLoader, asList("features"), asList((Object) "@nowhere"), new PrintStream(baos));

        assertEquals(String.format("None of the features at [features] matched the filters: [@nowhere]%n"), baos.toString());
    }
}
