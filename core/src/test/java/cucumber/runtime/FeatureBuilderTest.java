package cucumber.runtime;

import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureBuilderTest {

    @Test
    public void ignores_duplicate_features() throws IOException {
        List<CucumberFeature> features = new ArrayList<CucumberFeature>();
        FeatureBuilder builder = new FeatureBuilder(features);
        String featurePath = "foo.feature";
        Resource resource1 = createResourceMock(featurePath);
        Resource resource2 = createResourceMock(featurePath);

        builder.parse(resource1);
        builder.parse(resource2);

        assertEquals(1, features.size());
    }

    @Test
    public void works_when_path_and_uri_are_the_same() throws IOException {
        char fileSeparatorChar = '/';
        String featurePath = "path" + fileSeparatorChar + "foo.feature";
        Resource resource = createResourceMock(featurePath);
        List<CucumberFeature> features = new ArrayList<CucumberFeature>();
        FeatureBuilder builder = new FeatureBuilder(features, fileSeparatorChar);

        builder.parse(resource);

        assertEquals(1, features.size());
        assertEquals(featurePath, features.get(0).getPath());
    }

    @Test
    public void converts_windows_path_to_forward_slash() throws IOException {
        char fileSeparatorChar = '\\';
        String featurePath = "path" + fileSeparatorChar + "foo.feature";
        Resource resource = createResourceMock(featurePath);
        List<CucumberFeature> features = new ArrayList<CucumberFeature>();
        FeatureBuilder builder = new FeatureBuilder(features, fileSeparatorChar);

        builder.parse(resource);

        assertEquals(1, features.size());
        assertEquals("path/foo.feature", features.get(0).getPath());
    }

    private Resource createResourceMock(String featurePath) throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn(featurePath);
        ByteArrayInputStream feature = new ByteArrayInputStream("Feature: foo".getBytes("UTF-8"));
        when(resource.getInputStream()).thenReturn(feature);
        return resource;
    }

}
