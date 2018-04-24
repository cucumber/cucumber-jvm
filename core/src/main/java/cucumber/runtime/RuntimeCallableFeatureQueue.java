package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;

import java.util.Queue;

public class RuntimeCallableFeatureQueue extends RuntimeCallable {
    private final Queue<CucumberFeature> featureQueue;

    public RuntimeCallableFeatureQueue(final Runtime runtime, final Queue<CucumberFeature> featureQueue) {
        super(runtime);
        this.featureQueue = featureQueue;
    }

    @Override
    protected CucumberFeature poll() {
        return featureQueue.poll();
    }
}
