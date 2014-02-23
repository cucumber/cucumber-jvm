package cucumber.runtime.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Tag;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class FeatureRunnerTest {
    private FeatureRunner featureRunner;
    private CucumberFeature cucumberFeature;
    private Runtime runtime;
    private JUnitReporter jUnitReporter;
    private Feature feature = new Feature(Collections.<Comment>emptyList(), Collections.<Tag>emptyList(), "keyword", "name", "description", 1, "id");

    @Before
    public void setUp() throws Exception {
        cucumberFeature = mock(CucumberFeature.class);
        runtime = mock(Runtime.class);
        jUnitReporter = mock(JUnitReporter.class);
        featureRunner = new FeatureRunner(cucumberFeature, runtime, jUnitReporter);

    }

    @Test
    public void testRun() throws Exception {
        when(cucumberFeature.getGherkinFeature()).thenReturn(feature);
        when(cucumberFeature.getPath()).thenReturn("path");

        featureRunner.run(new RunNotifier());

        verify(cucumberFeature, times(4)).getGherkinFeature();
        verify(cucumberFeature, times(1)).getPath();
        verify(jUnitReporter, times(1)).uri(anyString());
        verify(jUnitReporter, times(1)).feature(any(Feature.class));
        verify(runtime, times(1)).runBeforeFeatureHooks(any(JUnitReporter.class), anySet());
        verify(runtime, times(1)).runAfterFeatureHooks(any(JUnitReporter.class), anySet());
        verify(jUnitReporter, times(1)).eof();
        verifyNoMoreInteractions(runtime, jUnitReporter);
    }
}
