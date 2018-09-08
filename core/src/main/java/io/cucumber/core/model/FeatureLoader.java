package io.cucumber.core.model;

import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.Resource;
import io.cucumber.core.io.ResourceLoader;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public final class FeatureLoader {

    private final ResourceLoader resourceLoader;

    public FeatureLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<CucumberFeature> load(List<String> featurePaths, PrintStream out) {
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

    public List<CucumberFeature> load(List<String> featurePaths) {
        final List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
        for (String featurePath : featurePaths) {
            loadFromFileSystemOrClasspath(builder, resourceLoader, featurePath);
        }
        cucumberFeatures.sort(new CucumberFeature.CucumberFeatureUriComparator());
        return cucumberFeatures;
    }

    private static void loadFromFileSystemOrClasspath(FeatureBuilder builder, ResourceLoader resourceLoader, String featurePath) {
        try {
            loadFromFeaturePath(builder, resourceLoader, featurePath, false);
        } catch (IllegalArgumentException originalException) {
            if (!featurePath.startsWith(MultiLoader.CLASSPATH_SCHEME) &&
                originalException.getMessage().contains("Not a file or directory")) {
                try {
                    loadFromFeaturePath(builder, resourceLoader, MultiLoader.CLASSPATH_SCHEME + featurePath, true);
                } catch (IllegalArgumentException secondException) {
                    if (secondException.getMessage().contains("No resource found for")) {
                        throw new IllegalArgumentException("Neither found on file system or on classpath: " +
                            originalException.getMessage() + ", " + secondException.getMessage());
                    } else {
                        throw secondException;
                    }
                }
            } else {
                throw originalException;
            }
        }
    }

    private static void loadFromFeaturePath(FeatureBuilder builder, ResourceLoader resourceLoader, String featurePath, boolean failOnNoResource) {
        Iterable<Resource> resources = resourceLoader.resources(featurePath, ".feature");
        if (failOnNoResource && !resources.iterator().hasNext()) {
            throw new IllegalArgumentException("No resource found for: " + featurePath);
        }
        for (Resource resource : resources) {
            builder.parse(resource);
        }
    }
}
