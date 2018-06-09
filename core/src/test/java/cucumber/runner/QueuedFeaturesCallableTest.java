package cucumber.runner;

import cucumber.runtime.FeatureRunner;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.LinkedList;
import java.util.Queue;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class QueuedFeaturesCallableTest {
    
    @Test
    public void should_run_until_no_more_features_to_consume() {
        final FeatureRunner featureRunner = mock(FeatureRunner.class);
        final Queue<CucumberFeature> queue = new LinkedList<CucumberFeature>();
        final CucumberFeature feature = mock(CucumberFeature.class);
        final CucumberFeature feature2 = mock(CucumberFeature.class);
        final CucumberFeature feature3 = mock(CucumberFeature.class);
        queue.add(feature);
        queue.add(feature2);
        queue.add(feature3);
        
        final QueuedFeaturesCallable callable = new QueuedFeaturesCallable(featureRunner, queue);
        callable.call();
        
        final InOrder order = inOrder(featureRunner);
        order.verify(featureRunner).runFeature(feature);
        order.verify(featureRunner).runFeature(feature2);
        order.verify(featureRunner).runFeature(feature3);
        order.verifyNoMoreInteractions();
    }
    
}