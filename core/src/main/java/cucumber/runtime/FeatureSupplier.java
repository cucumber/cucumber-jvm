package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;

import java.util.List;

public class FeatureSupplier implements Supplier<List<CucumberFeature>> {
    private final FeatureLoader featureLoader;
    private final RuntimeOptions runtimeOptions;

    public FeatureSupplier(FeatureLoader featureLoader, RuntimeOptions runtimeOptions) {
        this.featureLoader = featureLoader;
        this.runtimeOptions = runtimeOptions;
    }

    @Override
    public List<CucumberFeature> get() {
        return featureLoader.load(runtimeOptions.getFeaturePaths(), System.out);
    }
}
