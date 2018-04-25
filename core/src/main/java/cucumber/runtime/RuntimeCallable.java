package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;

import java.util.concurrent.Callable;

public abstract class RuntimeCallable implements Callable<Void> {
    private final Runtime runtime;

    public RuntimeCallable(final Runtime runtime) {
        this.runtime = runtime;
    }

    @Override
    public Void call() {
        CucumberFeature feature;
        while((feature = poll()) != null) {
            runtime.runFeature(feature);
        }
        return null;
    }

    protected abstract CucumberFeature poll();

}
