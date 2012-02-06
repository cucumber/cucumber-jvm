package cucumber.runtime.model;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.CucumberException;
import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberFeatureTest {
    @Test(expected = CucumberException.class)
    public void fails_if_no_features_are_found() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(anyString(), anyString())).thenReturn(Collections.<Resource>emptyList());
        CucumberFeature.load(resourceLoader, asList("does/not/exit"), emptyList());
    }
}
