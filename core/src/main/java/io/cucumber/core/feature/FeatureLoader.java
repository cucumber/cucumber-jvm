package io.cucumber.core.feature;

import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.resource.ResourceScanner;
import io.cucumber.core.resource.UriResourceScanner;

import java.net.URI;
import java.util.List;

import static io.cucumber.core.feature.FeatureIdentifier.isFeature;
import static io.cucumber.core.feature.FeatureParser.parseResource;
import static java.util.Optional.of;

public final class FeatureLoader {

    private final UriResourceScanner uriResourceScanner;

    public FeatureLoader(UriResourceScanner uriResourceScanner) {
        this.uriResourceScanner = uriResourceScanner;
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
        cucumberFeatures = featureScanner.scanForResourcesUri(featurePath, (resource) -> of(parseResource(resource)));
        if (isFeature(featurePath) && !cucumberFeatures.isEmpty()) {
            throw new IllegalArgumentException("Feature not found: " + featurePath);
        }

        builder.addAll(cucumberFeatures);
    }

}
