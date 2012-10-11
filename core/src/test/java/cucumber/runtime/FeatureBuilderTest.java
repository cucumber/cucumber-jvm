package cucumber.runtime;

import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureBuilderTest {

    public static final List<Object> NO_FILTERS = emptyList();

    @Test
    public void ignores_duplicate_features() throws IOException {
        List<CucumberFeature> features = new ArrayList<CucumberFeature>();
        FeatureBuilder builder = new FeatureBuilder(features);
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn("foo.feature");
        ByteArrayInputStream firstFeature = new ByteArrayInputStream("Feature: foo".getBytes("UTF-8"));
        ByteArrayInputStream secondFeature = new ByteArrayInputStream("Feature: foo".getBytes("UTF-8"));
        when(resource.getInputStream()).thenReturn(firstFeature, secondFeature);
        builder.parse(resource, NO_FILTERS);
        builder.parse(resource, NO_FILTERS);
        assertEquals(1, features.size());
    }

}
