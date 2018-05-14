package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;

import java.util.Queue;

public class RuntimeCallableFeatureGroupQueue extends RuntimeCallable {
    private final Queue<Queue<CucumberFeature>> featureGroupQueue;
    private Queue<CucumberFeature> currentGroup = null;

    public RuntimeCallableFeatureGroupQueue(final Runtime runtime, final Queue<Queue<CucumberFeature>> featureGroupQueue) {
        super(runtime);
        this.featureGroupQueue = featureGroupQueue;
    }

    @Override
    protected CucumberFeature poll() {
        CucumberFeature feature = null;
        if (currentGroup == null || (feature = currentGroup.poll()) == null) {
            currentGroup = featureGroupQueue.poll();
            if (currentGroup != null) {
                feature = currentGroup.poll();
            }
        }
        return feature;
    }
}
