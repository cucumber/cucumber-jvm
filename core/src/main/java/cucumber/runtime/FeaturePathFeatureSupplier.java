package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import cucumber.util.FixJava;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.FeatureOptions;

import java.net.URI;
import java.util.List;

/**
 * Supplies a list of features found on the the feature path provided to RuntimeOptions.
 */
public class FeaturePathFeatureSupplier implements FeatureSupplier {

    private static final Logger log = LoggerFactory.getLogger(FeaturePathFeatureSupplier.class);

    private final FeatureLoader featureLoader;
    private final FeatureOptions featureOptions;

    public FeaturePathFeatureSupplier(FeatureLoader featureLoader, FeatureOptions featureOptions) {
        this.featureLoader = featureLoader;
        this.featureOptions = featureOptions;
    }

    @Override
    public List<CucumberFeature> get() {
        List<URI> featurePaths = featureOptions.getFeaturePaths();

        log.debug("Loading features from " + FixJava.join(featurePaths, ", "));
        List<CucumberFeature> cucumberFeatures = featureLoader.load(featurePaths);

        if (cucumberFeatures.isEmpty()) {
            if (featurePaths.isEmpty()) {
                log.warn("Got no path to feature directory or feature file");
            } else {
                log.warn("No features found at " + FixJava.join(featurePaths, ", "));
            }
        }

        return cucumberFeatures;
    }
}
