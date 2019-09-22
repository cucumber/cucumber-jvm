package io.cucumber.core.runtime;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.FeatureLoader;
import io.cucumber.core.feature.Options;

import java.net.URI;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Supplies a list of features found on the the feature path provided to RuntimeOptions.
 */
public final class FeaturePathFeatureSupplier implements FeatureSupplier {

    private static final Logger log = LoggerFactory.getLogger(FeaturePathFeatureSupplier.class);

    private final FeatureLoader featureLoader;
    private final Options featureOptions;

    public FeaturePathFeatureSupplier(FeatureLoader featureLoader, Options featureOptions) {
        this.featureLoader = featureLoader;
        this.featureOptions = featureOptions;
    }

    @Override
    public List<CucumberFeature> get() {
        List<URI> featurePaths = featureOptions.getFeaturePaths();

        log.debug("Loading features from " + featurePaths.stream().map(URI::toString).collect(joining(", ")));
        List<CucumberFeature> cucumberFeatures = featureLoader.load(featurePaths);

        if (cucumberFeatures.isEmpty()) {
            if (featurePaths.isEmpty()) {
                log.warn("Got no path to feature directory or feature file");
            } else {
                log.warn("No features found at " + featurePaths.stream().map(URI::toString).collect(joining(", ")));
            }
        }

        return cucumberFeatures;
    }
}
