package io.cucumber.core.model;


import io.cucumber.core.io.Resource;
import io.cucumber.core.io.ResourceLoader;

import java.io.PrintStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

public final class FeatureLoader {

    private static final String FEATURE_SUFFIX = ".feature";
    private final ResourceLoader resourceLoader;

    public FeatureLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<CucumberFeature> load(List<URI> featurePaths, PrintStream out) {
        final List<CucumberFeature> cucumberFeatures = load(featurePaths);
        if (cucumberFeatures.isEmpty()) {
            if (featurePaths.isEmpty()) {
                out.println("Got no path to feature directory or feature file");
            } else {
                out.println(String.format("No features found at %s", featurePaths));
            }
        }
        return cucumberFeatures;
    }

    public List<CucumberFeature> load(List<URI> featurePaths) {
        final FeatureBuilder builder = new FeatureBuilder();
        for (URI featurePath : featurePaths) {
            loadFromFeaturePath(builder, featurePath);
        }
        return builder.build();
    }

    private void loadFromFeaturePath(FeatureBuilder builder, URI featurePath) {
        Iterable<Resource> resources = resourceLoader.resources(featurePath, FEATURE_SUFFIX);

        Iterator<Resource> iterator = resources.iterator();
        if (featurePath.getSchemeSpecificPart().endsWith(FEATURE_SUFFIX) && !iterator.hasNext()) {
            throw new IllegalArgumentException("Feature not found: " + featurePath);
        }
        while (iterator.hasNext()) {
            builder.parse(iterator.next());
        }
    }

}
