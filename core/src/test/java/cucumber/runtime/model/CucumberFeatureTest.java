package cucumber.runtime.model;

import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberFeatureTest {
    @Test
    public void succeds_if_no_features_are_found() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.<Resource>emptyList());

        CucumberFeature.load(resourceLoader, singletonList("does/not/exist"), new PrintStream(new ByteArrayOutputStream()));
    }

    @Test
    public void logs_message_if_no_features_are_found() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.<Resource>emptyList());

        CucumberFeature.load(resourceLoader, singletonList("does/not/exist"), new PrintStream(baos));

        assertEquals(String.format("No features found at [does/not/exist]%n"), baos.toString());
    }

    @Test
    public void logs_message_if_no_feature_paths_are_given() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);

        CucumberFeature.load(resourceLoader, Collections.<String>emptyList(), new PrintStream(baos));

        assertEquals(String.format("Got no path to feature directory or feature file%n"), baos.toString());
    }

    @Test
    public void loads_features_specified_in_rerun_file() throws Exception {
        String featurePath1 = "path/bar.feature";
        String feature1 = "" +
                "Feature: bar\n" +
                "  Scenario: scenario bar\n" +
                "    * step\n";
        String featurePath2 = "path/foo.feature";
        String feature2 = "" +
                "Feature: foo\n" +
                "  Scenario: scenario 1\n" +
                "    * step\n" +
                "  Scenario: scenario 2\n" +
                "    * step\n";
        String rerunPath = "path/rerun.txt";
        String rerunFile = featurePath1 + ":2 " + featurePath2 + ":4";
        ResourceLoader resourceLoader = mockFeatureFileResource(featurePath1, feature1);
        mockFeatureFileResource(resourceLoader, featurePath2, feature2);
        mockFileResource(resourceLoader, rerunPath, null, rerunFile);

        List<CucumberFeature> features = CucumberFeature.load(
                resourceLoader,
                singletonList("@" + rerunPath),
                new PrintStream(new ByteArrayOutputStream()));

        assertEquals(2, features.size());
        assertEquals(1, features.get(0).getGherkinFeature().getFeature().getChildren().size());
        assertEquals("scenario bar", features.get(0).getGherkinFeature().getFeature().getChildren().get(0).getName());
        assertEquals(2, features.get(1).getGherkinFeature().getFeature().getChildren().size());
        assertEquals("scenario 1", features.get(1).getGherkinFeature().getFeature().getChildren().get(0).getName());
        assertEquals("scenario 2", features.get(1).getGherkinFeature().getFeature().getChildren().get(1).getName());
    }

    @Test
    public void loads_no_features_when_rerun_file_is_empty() throws Exception {
        String feature = "" +
                "Feature: bar\n" +
                "  Scenario: scenario bar\n" +
                "    * step\n";
        String rerunPath = "path/rerun.txt";
        String rerunFile = "";
        ResourceLoader resourceLoader = mockFeatureFileResourceForAnyFeaturePath(feature);
        mockFileResource(resourceLoader, rerunPath, null, rerunFile);

        List<CucumberFeature> features = CucumberFeature.load(
                resourceLoader,
                singletonList("@" + rerunPath),
                new PrintStream(new ByteArrayOutputStream()));

        assertEquals(0, features.size());
    }

    @Test
    public void loads_features_specified_in_rerun_file_from_classpath_when_not_in_file_system() throws Exception {
        String featurePath = "path/bar.feature";
        String feature = "" +
                "Feature: bar\n" +
                "  Scenario: scenario bar\n" +
                "    * step\n";
        String rerunPath = "path/rerun.txt";
        String rerunFile = featurePath + ":2";
        ResourceLoader resourceLoader = mockFeatureFileResource("classpath:" + featurePath, feature);
        mockFeaturePathToNotExist(resourceLoader, featurePath);
        mockFileResource(resourceLoader, rerunPath, suffix(null), rerunFile);

        List<CucumberFeature> features = CucumberFeature.load(
                resourceLoader,
                singletonList("@" + rerunPath),
                new PrintStream(new ByteArrayOutputStream()));

        assertEquals(1, features.size());
        assertEquals(1, features.get(0).getGherkinFeature().getFeature().getChildren().size());
        assertEquals("scenario bar", features.get(0).getGherkinFeature().getFeature().getChildren().get(0).getName());
    }

    @Test
    public void gives_error_message_if_path_from_rerun_file_does_not_exist() throws Exception {
        String featurePath = "path/bar.feature";
        String rerunPath = "path/rerun.txt";
        String rerunFile = featurePath + ":2";
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        mockFeaturePathToNotExist(resourceLoader, featurePath);
        mockFeaturePathToNotExist(resourceLoader, "classpath:" + featurePath);
        mockFileResource(resourceLoader, rerunPath, suffix(null), rerunFile);

        try {
            CucumberFeature.load(
                    resourceLoader,
                    singletonList("@" + rerunPath),
                    new PrintStream(new ByteArrayOutputStream()));
            fail("IllegalArgumentException was expected");
        } catch (IllegalArgumentException exception) {
            assertEquals("Neither found on file system or on classpath: " +
                            "Not a file or directory: path/bar.feature, No resource found for: classpath:path/bar.feature",
                    exception.getMessage());
        }
    }

    private ResourceLoader mockFeatureFileResource(String featurePath, String feature)
            throws IOException {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        mockFeatureFileResource(resourceLoader, featurePath, feature);
        return resourceLoader;
    }

    private ResourceLoader mockFeatureFileResourceForAnyFeaturePath(String feature)
            throws IOException {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn("");
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(feature.getBytes("UTF-8")));
        when(resourceLoader.resources(anyString(), anyString())).thenReturn(singletonList(resource));
        return resourceLoader;
    }

    private void mockFeatureFileResource(ResourceLoader resourceLoader, String featurePath, String feature)
            throws IOException {
        mockFileResource(resourceLoader, featurePath, ".feature", feature);
    }

    private void mockFileResource(ResourceLoader resourceLoader, String featurePath, String extension, String feature)
            throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn(featurePath);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(feature.getBytes("UTF-8")));
        when(resourceLoader.resources(featurePath, extension)).thenReturn(singletonList(resource));
    }

    private void mockFeaturePathToNotExist(ResourceLoader resourceLoader, String featurePath) {
        if (featurePath.startsWith("classpath")) {
            when(resourceLoader.resources(featurePath, ".feature")).thenReturn(new ArrayList<Resource>());
        } else {
            when(resourceLoader.resources(featurePath, ".feature")).thenThrow(new IllegalArgumentException("Not a file or directory: " + featurePath));
        }
    }

    private String suffix(String suffix) {
        return suffix;
    }
}
