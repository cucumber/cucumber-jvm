package io.cucumber.core.model;

import io.cucumber.core.io.Resource;
import io.cucumber.core.io.ResourceLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
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
        when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.emptyList());

        new FeatureLoader(resourceLoader).load(singletonList("does/not/exist"), printStream);
    }

    @Test
    public void logs_message_if_no_features_are_found() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.emptyList());

        new FeatureLoader(resourceLoader).load(singletonList("does/not/exist"), new PrintStream(baos));

        assertEquals(String.format("No features found at [does/not/exist]%n"), baos.toString());
    }

    @Test
    public void logs_message_if_no_feature_paths_are_given() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);

        new FeatureLoader(resourceLoader).load(Collections.emptyList(), new PrintStream(baos));

        assertEquals(String.format("Got no path to feature directory or feature file%n"), baos.toString());
    }

    @Test
    public void loads_features_specified_in_rerun_file_from_classpath_when_not_in_file_system() throws Exception {
        String featurePath = "path/bar.feature";
        String feature = "" +
            "Feature: bar\n" +
            "  Scenario: scenario bar\n" +
            "    * step\n";
        ResourceLoader resourceLoader = mockFeatureFileResource("classpath:" + featurePath, feature);
        mockFeaturePathToNotExist(resourceLoader, featurePath);

        List<CucumberFeature> features = new FeatureLoader(resourceLoader).load(singletonList(featurePath), printStream);

        assertEquals(1, features.size());
        assertEquals(1, features.get(0).getGherkinFeature().getFeature().getChildren().size());
        assertEquals("scenario bar", features.get(0).getGherkinFeature().getFeature().getChildren().get(0).getName());
    }

    @Test
    public void gives_error_message_if_path_from_rerun_file_does_not_exist() {
        String featurePath = "path/bar.feature";
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        mockFeaturePathToNotExist(resourceLoader, featurePath);
        mockFeaturePathToNotExist(resourceLoader, "classpath:" + featurePath);

        expectedException.expectMessage(
            "Neither found on file system or on classpath: " +
                "Not a file or directory: path/bar.feature, No resource found for: classpath:path/bar.feature");
        new FeatureLoader(resourceLoader).load(singletonList(featurePath), printStream);
    }


    private ResourceLoader mockFeatureFileResource(String featurePath, String feature) throws IOException {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        mockFeatureFileResource(resourceLoader, featurePath, feature);
        return resourceLoader;
    }

    private void mockFeatureFileResource(ResourceLoader resourceLoader, String featurePath, String feature)
        throws IOException {
        mockFileResource(resourceLoader, featurePath, feature);
    }

    private void mockFileResource(ResourceLoader resourceLoader, String path, String contents) throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn(path);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(contents.getBytes(UTF_8)));
        when(resourceLoader.resources(path, ".feature")).thenReturn(singletonList(resource));
    }

    private void mockFeaturePathToNotExist(ResourceLoader resourceLoader, String featurePath) {
        if (featurePath.startsWith("classpath")) {
            when(resourceLoader.resources(featurePath, ".feature")).thenReturn(new ArrayList<>());
        } else {
            when(resourceLoader.resources(featurePath, ".feature")).thenThrow(new IllegalArgumentException("Not a file or directory: " + featurePath));
        }
    }

}
