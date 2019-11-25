package io.cucumber.core.runtime;

import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.FeatureIdentifier;
import io.cucumber.core.feature.Options;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.resource.ResourceScanner;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.cucumber.core.feature.FeatureIdentifier.isFeature;
import static io.cucumber.core.feature.FeatureParser.parseResource;
import static java.util.Comparator.comparing;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;

/**
 * Supplies a list of features found on the the feature path provided to RuntimeOptions.
 */
public final class FeaturePathFeatureSupplier implements FeatureSupplier {

    private static final Logger log = LoggerFactory.getLogger(FeaturePathFeatureSupplier.class);

    private final ResourceScanner<CucumberFeature> featureScanner;

    private final Options featureOptions;

    public FeaturePathFeatureSupplier(Supplier<ClassLoader> classLoader, Options featureOptions) {
        this.featureOptions = featureOptions;
        this.featureScanner = new ResourceScanner<>(
            classLoader,
            FeatureIdentifier::isFeature,
            resource -> of(parseResource(resource))
        );
    }

    @Override
    public List<CucumberFeature> get() {
        List<URI> featurePaths = featureOptions.getFeaturePaths();
        List<CucumberFeature> features = loadFeatures(featurePaths);
        if (features.isEmpty()) {
            if (featurePaths.isEmpty()) {
                log.warn(() -> "Got no path to feature directory or feature file");
            } else {
                log.warn(() -> "No features found at " + featurePaths.stream().map(URI::toString).collect(joining(", ")));
            }
        }
        return features;
    }

    private List<CucumberFeature> loadFeatures(List<URI> featurePaths) {
        log.debug(() -> "Loading features from " + featurePaths.stream().map(URI::toString).collect(joining(", ")));
        final FeatureBuilder builder = new FeatureBuilder();

        for (URI featurePath : featurePaths) {
            List<CucumberFeature> found = featureScanner.scanForResourcesUri(featurePath);
            if (found.isEmpty() && isFeature(featurePath)) {
                throw new IllegalArgumentException("Feature not found: " + featurePath);
            }
            found.forEach(builder::addUnique);
        }

        return builder.build();
    }

    static final class FeatureBuilder {

        private final Map<String, Map<String, CucumberFeature>> sourceToFeature = new HashMap<>();
        private final List<CucumberFeature> features = new ArrayList<>();

        List<CucumberFeature> build() {
            List<CucumberFeature> cucumberFeatures = new ArrayList<>(features);
            cucumberFeatures.sort(comparing(CucumberFeature::getUri));
            return cucumberFeatures;
        }

        void addUnique(CucumberFeature parsedFeature) {
            String parsedFileName = getFileName(parsedFeature);

            Map<String, CucumberFeature> existingFeatures = sourceToFeature.get(parsedFeature.getSource());
            if (existingFeatures != null) {
                // Same contents but different file names was probably intentional
                CucumberFeature existingFeature = existingFeatures.get(parsedFileName);
                if (existingFeature != null) {
                    log.error(() -> "" +
                        "Duplicate feature found: " +
                        parsedFeature.getUri() + " was identical to " + existingFeature.getUri() + "\n" +
                        "\n" +
                        "This typically happens when you configure cucumber to look " +
                        "for features in the root of your project.\nYour build tool " +
                        "creates a copy of these features in a 'target' or 'build'" +
                        "directory.\n" +
                        "\n" +
                        "If your features are on the class path consider using a class path URI.\n" +
                        "For example: 'classpath:com/example/app.feature'\n" +
                        "Otherwise you'll have to provide a more specific location"
                    );
                    return;
                }
            }
            sourceToFeature.putIfAbsent(parsedFeature.getSource(), new HashMap<>());
            sourceToFeature.get(parsedFeature.getSource()).put(parsedFileName, parsedFeature);
            features.add(parsedFeature);
        }

        private String getFileName(CucumberFeature feature) {
            String uri = feature.getUri().getSchemeSpecificPart();
            int i = uri.lastIndexOf("/");
            return i > 0 ? uri.substring(i) : uri;
        }
    }

}
