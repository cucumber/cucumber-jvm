package io.cucumber.core.feature;

import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.resource.Classpath;
import io.cucumber.core.resource.ResourceScanner;

import java.net.URI;
import java.util.List;

import static java.util.Optional.of;

public final class FeatureLoader {

    private final ResourceScanner<CucumberFeature> featureScanner = new ResourceScanner<>(
        ClassLoaders::getDefaultClassLoader,
        FeatureIdentifier::isFeature,
        (resource) -> of(FeatureParser.parseResource(resource))
    );

    public FeatureLoader() {

    }

    public List<CucumberFeature> load(List<URI> featurePaths) {
        final FeatureBuilder builder = new FeatureBuilder();
        for (URI featurePath : featurePaths) {
            loadFromFeaturePath(builder, featurePath);
        }
        return builder.build();
    }

    private void loadFromFeaturePath(FeatureBuilder builder, URI featurePath) {
        List<CucumberFeature> cucumberFeatures;
        if (Classpath.CLASSPATH_SCHEME.equals(featurePath.getScheme())) {
            String resourcePath = Classpath.resourceName(featurePath);
            cucumberFeatures = featureScanner.scanForClasspathResource(resourcePath, s -> true);
        } else {
            cucumberFeatures = featureScanner.scanForResourcesUri(featurePath);
        }
        if (FeatureIdentifier.isFeature(featurePath) && !cucumberFeatures.isEmpty()) {
            throw new IllegalArgumentException("Feature not found: " + featurePath);
        }

        builder.addAll(cucumberFeatures);
    }

}
