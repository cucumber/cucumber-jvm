package io.cucumber.core.runtime;

import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.feature.Options;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.resource.Resource;
import io.cucumber.core.resource.ResourceScanner;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cucumber.core.feature.FeatureIdentifier.isFeature;
import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;
import static io.cucumber.core.resource.ClasspathSupport.resourcePath;
import static java.util.stream.Collectors.joining;

/**
 * Supplies a list of features found on the the feature path provided to RuntimeOptions.
 */
public final class FeaturePathFeatureSupplier implements FeatureSupplier {

    private static final Logger log = LoggerFactory.getLogger(FeaturePathFeatureSupplier.class);

    private final ResourceScanner<CucumberFeature> featureScanner = new ResourceScanner<>(
        ClassLoaders::getDefaultClassLoader,
        FeatureIdentifier::isFeature,
        FeatureParser::parseResource
    );

    private final Options featureOptions;

    public FeaturePathFeatureSupplier(Options featureOptions) {
        this.featureOptions = featureOptions;
    }

    @Override
    public List<CucumberFeature> get() {
        List<URI> featurePaths = featureOptions.getFeaturePaths();

        log.debug(() -> "Loading features from " + featurePaths.stream().map(URI::toString).collect(joining(", ")));
        final FeatureBuilder builder = new FeatureBuilder();

        for (URI featurePath : featurePaths) {
            List<CucumberFeature> found;
            if (CLASSPATH_SCHEME.equals(featurePath.getScheme())) {
                String resourcePath = resourcePath(featurePath);
                found = featureScanner.scanForClasspathResource(resourcePath, s -> true);
            } else {
                found = featureScanner.scanForResourcesUri(featurePath);
            }

            if (found.isEmpty() && isFeature(featurePath)) {
                throw new IllegalArgumentException("Feature not found: " + featurePath);
            }
            builder.addAll(found);
        }

        List<CucumberFeature> cucumberFeatures = builder.build();

        if (cucumberFeatures.isEmpty()) {
            if (featurePaths.isEmpty()) {
                log.warn(() -> "Got no path to feature directory or feature file");
            } else {
                log.warn(() -> "No features found at " + featurePaths.stream().map(URI::toString).collect(joining(", ")));
            }
        }

        return cucumberFeatures;
    }


    static final class FeatureBuilder {

        private final Map<String, CucumberFeature> sourceToFeature = new HashMap<>();

        public List<CucumberFeature> build() {
            List<CucumberFeature> cucumberFeatures = new ArrayList<>(sourceToFeature.values());
            cucumberFeatures.sort(new CucumberFeature.CucumberFeatureUriComparator());
            return cucumberFeatures;
        }

        public void parse(Resource resource) {
            CucumberFeature parsedFeature = FeatureParser.parseResource(resource);
            addUniqueFeatures(parsedFeature);
        }

        private void addUniqueFeatures(CucumberFeature parsedFeature) {
            CucumberFeature existingFeature = sourceToFeature.get(parsedFeature.getSource());
            if (existingFeature != null) {
                log.warn(() -> "Duplicate feature ignored. " + parsedFeature.getUri() + " was identical to " + existingFeature.getUri());
                return;
            }
            sourceToFeature.put(parsedFeature.getSource(), parsedFeature);
        }

        public void addAll(List<CucumberFeature> cucumberFeatures) {
            cucumberFeatures.forEach(this::addUniqueFeatures);
        }
    }
}
