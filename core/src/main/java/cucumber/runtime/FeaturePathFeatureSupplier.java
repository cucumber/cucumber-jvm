package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import io.cucumber.core.options.FeatureOptions;

import java.util.List;

/**
 * Supplies a list of features found on the the feature path provided to RuntimeOptions.
 */
public class FeaturePathFeatureSupplier implements FeatureSupplier {
    private final FeatureLoader featureLoader;
    private final FeatureOptions featureOptions;

    public FeaturePathFeatureSupplier(FeatureLoader featureLoader, FeatureOptions featureOptions) {
        this.featureLoader = featureLoader;
        this.featureOptions = featureOptions;
    }

    @Override
    public List<CucumberFeature> get() {
        return featureLoader.load(featureOptions.getFeaturePaths(), System.out);
    }
}
