package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;

import java.util.List;

/**
 * Supplies a list of features found on the the feature path provided to RuntimeOptions.
 */
public class FeaturePathFeatureSupplier implements FeatureSupplier {
    private final FeatureLoader featureLoader;
    private final RuntimeOptions runtimeOptions;

    public FeaturePathFeatureSupplier(FeatureLoader featureLoader, RuntimeOptions runtimeOptions) {
        this.featureLoader = featureLoader;
        this.runtimeOptions = runtimeOptions;
    }

    @Override
    public List<CucumberFeature> get() {
        return featureLoader.load(runtimeOptions.getFeaturePaths(), System.out);
    }
}
