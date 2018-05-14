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
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class RuntimeCallableFeatureGroupQueueTest {

    @Test
    public void should_run_until_no_more_features_to_consume() {
        final Runtime runtime = mock(Runtime.class);
        final Queue<Queue<CucumberFeature>> allQueues = new LinkedList<Queue<CucumberFeature>>();

        final Queue<CucumberFeature> queue1 = new LinkedList<CucumberFeature>();        
        final CucumberFeature feature = mock(CucumberFeature.class);
        final CucumberFeature feature2 = mock(CucumberFeature.class);
        queue1.add(feature);
        queue1.add(feature2);
        allQueues.add(queue1);

        final Queue<CucumberFeature> queue2 = new LinkedList<CucumberFeature>();
        final CucumberFeature feature3 = mock(CucumberFeature.class);
        queue2.add(feature3);
        allQueues.add(queue2);

        final RuntimeCallableFeatureGroupQueue callable = new RuntimeCallableFeatureGroupQueue(runtime, allQueues);

        callable.call();

        final InOrder order = inOrder(runtime);
        order.verify(runtime).runFeature(feature);
        order.verify(runtime).runFeature(feature2);
        order.verify(runtime).runFeature(feature3);
        verifyNoMoreInteractions(runtime);
    }
    
}