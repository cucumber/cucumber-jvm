package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;

import java.util.Queue;
import java.util.concurrent.Callable;

public class RuntimeCallable implements Callable<Void> {
    private final Runtime runtime;
    private final Queue<CucumberFeature> featureQueue;

    public RuntimeCallable(final Runtime runtime, final Queue<CucumberFeature> featureQueue) {
        this.runtime = runtime;
        this.featureQueue = featureQueue;
    }

    @Override
    public Void call() {
        runtime.prepareForFeatureRun();
        CucumberFeature feature;
        while((feature = featureQueue.poll()) != null) {
            runtime.runFeature(feature);
        }
        return null;
    }
}
