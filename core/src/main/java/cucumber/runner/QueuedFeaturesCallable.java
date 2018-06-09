package cucumber.runner;

import cucumber.runtime.FeatureRunner;
import cucumber.runtime.model.CucumberFeature;

import java.util.Queue;
import java.util.concurrent.Callable;

public class QueuedFeaturesCallable implements Callable<Void> {
    private final FeatureRunner runner;
    private final Queue<CucumberFeature> featureQueue;

    public QueuedFeaturesCallable(final FeatureRunner runner, final Queue<CucumberFeature> featureQueue) {
        this.runner = runner;
        this.featureQueue = featureQueue;
    }

    @Override
    public Void call() {
        CucumberFeature feature;
        while ((feature = featureQueue.poll()) != null) {
            runner.runFeature(feature);
        }
        return null;
    }
}
