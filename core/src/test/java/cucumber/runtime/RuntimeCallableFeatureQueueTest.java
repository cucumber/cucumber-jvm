package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.Queue;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class RuntimeCallableFeatureQueueTest {
    
    @Test
    public void should_run_until_no_more_features_to_consume() {
        final Runtime runtime = mock(Runtime.class);
        final Queue<CucumberFeature> queue = new LinkedList<CucumberFeature>();
        final CucumberFeature feature = mock(CucumberFeature.class);
        final CucumberFeature feature2 = mock(CucumberFeature.class);
        queue.add(feature);
        queue.add(feature2);
        
        final RuntimeCallableFeatureQueue callable = new RuntimeCallableFeatureQueue(runtime, queue);
        
        callable.call();

        final InOrder order = inOrder(runtime);
        order.verify(runtime).prepareForFeatureRun();
        order.verify(runtime).runFeature(feature);
        order.verify(runtime).runFeature(feature2);
        verifyNoMoreInteractions(runtime);
    }
    
}