package cucumber.runtime;

import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;

import java.util.List;

public class FeatureSupplier implements Supplier<List<CucumberFeature>> {
    private final ResourceLoader resourceLoader;
    private final RuntimeOptions runtimeOptions;

    public FeatureSupplier(ResourceLoader resourceLoader, RuntimeOptions runtimeOptions) {
        this.resourceLoader = resourceLoader;
        this.runtimeOptions = runtimeOptions;
    }

    @Override
    public List<CucumberFeature> get() {
        return new FeatureLoader(resourceLoader).load(runtimeOptions.getFeaturePaths(), System.out);
    }
}
