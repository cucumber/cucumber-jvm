package cucumber.runtime;

import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.FeatureLoader;
import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.model.FeaturePath;
import io.cucumber.core.options.FeatureOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeaturePathFeatureSupplierTest {

    private LogRecordListener logRecordListener;

    @Before
    public void setup() {
        logRecordListener = new LogRecordListener();
        LoggerFactory.addListener(logRecordListener);
    }

    @After
    public void tearDown(){
        LoggerFactory.removeListener(logRecordListener);
    }

    @Test
    public void logs_message_if_no_features_are_found() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(URI.create("file:does/not/exist"), ".feature")).thenReturn(Collections.<Resource>emptyList());
        FeatureOptions featureOptions = new FeatureOptions() {
            @Override
            public List<URI> getFeaturePaths() {
                return Collections.singletonList(FeaturePath.parse("does/not/exist"));
            }
        };

        FeaturePathFeatureSupplier supplier = new FeaturePathFeatureSupplier(new FeatureLoader(resourceLoader), featureOptions);
        supplier.get();
        assertThat(logRecordListener.getLogRecords().get(1).getMessage(), containsString("No features found at file:does/not/exist"));
    }

    @Test
    public void logs_message_if_no_feature_paths_are_given() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        FeatureOptions featureOptions = new FeatureOptions() {
            @Override
            public List<URI> getFeaturePaths() {
                return Collections.emptyList();
            }
        };

        FeaturePathFeatureSupplier supplier = new FeaturePathFeatureSupplier(new FeatureLoader(resourceLoader), featureOptions);
        supplier.get();
        assertThat(logRecordListener.getLogRecords().get(1).getMessage(), containsString("Got no path to feature directory or feature file"));
    }

}
