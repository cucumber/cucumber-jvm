package cucumber.runtime;

import cucumber.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureBuilderTest {
    @Test(expected = CucumberException.class)
    public void detects_duplicate_features() throws IOException {
        List<CucumberFeature> fearures = new ArrayList<CucumberFeature>();
        FeatureBuilder builder = new FeatureBuilder(fearures);
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn("foo.feature");
        ByteArrayInputStream firstFeature = new ByteArrayInputStream("Feature: foo".getBytes("UTF-8"));
        ByteArrayInputStream secondFeature = new ByteArrayInputStream("Feature: foo".getBytes("UTF-8"));
        when(resource.getInputStream()).thenReturn(firstFeature, secondFeature);
        builder.parse(resource, emptyList());
        builder.parse(resource, emptyList());
    }

}
