package cucumber.runtime;

import java.util.List;
import java.util.concurrent.Callable;

import cucumber.runtime.model.CucumberFeature;

public class RuntimeCallable implements Callable<Void> {
    private final Runtime runtime;
    private final List<CucumberFeature> featureSet;

    public RuntimeCallable(final Runtime runtime, final List<CucumberFeature> featureSet) {
        this.runtime = runtime;
        this.featureSet = featureSet;
    }

    @Override
    public Void call() {
        for (final CucumberFeature feature : featureSet) {
            runtime.runFeature(feature);
        }
        return null;
    }
}
