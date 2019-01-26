package io.cucumber.core.model;

import io.cucumber.core.io.ResourceLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberFeatureTest {

    private final PrintStream printStream = new PrintStream(new ByteArrayOutputStream());
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void succeeds_if_no_features_are_found() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(URI.create("does/not/exist"), ".feature")).thenReturn(Collections.emptyList());
        new FeatureLoader(resourceLoader).load(singletonList(URI.create("does/not/exist")), printStream);
    }

    @Test
    public void logs_message_if_no_features_are_found() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(URI.create("does/not/exist"), ".feature")).thenReturn(Collections.emptyList());
        new FeatureLoader(resourceLoader).load(singletonList(URI.create("does/not/exist")), new PrintStream(baos));

        assertEquals(String.format("No features found at [does/not/exist]%n"), baos.toString());
    }

    @Test
    public void logs_message_if_no_feature_paths_are_given() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);

        new FeatureLoader(resourceLoader).load(Collections.emptyList(), new PrintStream(baos));

        assertEquals(String.format("Got no path to feature directory or feature file%n"), baos.toString());
    }
}
