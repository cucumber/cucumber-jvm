package cucumber.runner;

import cucumber.runtime.FeatureRunner;
import cucumber.runtime.model.CucumberFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParallelFeatureRunner {

    private ParallelFeatureRunner() {
        // Hide constructor
    }

    public static void run(final FeatureRunner featureRunner,
                           final List<CucumberFeature> features,
                           final int requestedThreads) {
        final ConcurrentLinkedQueue<CucumberFeature> queuedFeatures = new ConcurrentLinkedQueue<CucumberFeature>(features);
        final int threadCount = Math.min(requestedThreads, features.size());
        final List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            tasks.add(new QueuedFeaturesCallable(featureRunner, queuedFeatures));
        }
        new ParallelCallableExecutor<Void>().run(tasks);
    }
}
