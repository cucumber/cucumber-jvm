package cucumber.runtime.model;

import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.SingleFeatureBuilder;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;

import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.*;

public class CucumberFeaturesLoader<T> {

    private List<String> featurePaths;
    private List<Object> filters;
    private Class<T> resultType;

    public CucumberFeaturesLoader(List<String> featurePaths, List<Object> filters, Class<T> resultClass) {
        this.featurePaths = featurePaths;
        this.filters = filters;
        this.resultType = resultClass;
    }

    public List<T> load(ResourceLoader resourceLoader, PrintStream out) {
        final List<T> cucumberFeatures = load(resourceLoader);
        if (cucumberFeatures.isEmpty()) {
            if (featurePaths.isEmpty()) {
                out.println(String.format("Got no path to feature directory or feature file"));
            } else if (filters.isEmpty()) {
                out.println(String.format("No features found at %s", featurePaths));
            } else {
                out.println(String.format("None of the features at %s matched the filters: %s", featurePaths, filters));
            }
        }
        return cucumberFeatures;
    }

    @SuppressWarnings("unchecked")
    public List<T> load(ResourceLoader resourceLoader) {
        List<T> cucumberFeatures = new ArrayList<T>();
        for (String featurePath : this.featurePaths) {
            if (featurePath.startsWith("@")) {
                cucumberFeatures.addAll(loadFromRerunFile(resourceLoader, featurePath.substring(1)));
            } else {
                cucumberFeatures.addAll(loadFromFeaturePath(resourceLoader, featurePath, false));
            }
        }
        Collections.sort(cucumberFeatures, new CommonUriComparator<T>());
        return cucumberFeatures;
    }

    private List<T> loadFromRerunFile(ResourceLoader resourceLoader, String rerunPath) {
        Iterable<Resource> resources = resourceLoader.resources(rerunPath, null);
        List<T> cucumberFeatures = new ArrayList<T>();
        if (resultType.equals(CucumberFeature.class)) {
            for (Resource resource : resources) {
                String source = FeatureBuilder.read(resource);
                if (!source.isEmpty()) {
                    for (String featurePath : source.split(" ")) {
                        cucumberFeatures.addAll(loadFromFileSystemOrClasspath(resourceLoader, featurePath));
                    }
                }
            }
        } else {
            for (Resource resource : resources) {
                cucumberFeatures.add((T) resource);
            }
        }
        return cucumberFeatures;
    }

    private List<T> loadFromFileSystemOrClasspath(ResourceLoader resourceLoader, String featurePath) {
        try {
            return loadFromFeaturePath(resourceLoader, featurePath, false);
        } catch (IllegalArgumentException originalException) {
            if (!featurePath.startsWith(MultiLoader.CLASSPATH_SCHEME) &&
                    originalException.getMessage().contains("Not a file or directory")) {
                try {
                    return loadFromFeaturePath(resourceLoader, MultiLoader.CLASSPATH_SCHEME + featurePath, true);
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

    private List<T> loadFromFeaturePath(ResourceLoader resourceLoader, String featurePath, boolean failOnNoResource) {
        List<T> cucumberFeatures = new ArrayList<T>();

        PathWithLines pathWithLines = new PathWithLines(featurePath);
        ArrayList<Object> filtersForPath = new ArrayList<Object>(this.filters);
        filtersForPath.addAll(pathWithLines.lines);
        Iterable<Resource> resources = resourceLoader.resources(pathWithLines.path, ".feature");
        if (failOnNoResource && !resources.iterator().hasNext()) {
            throw new IllegalArgumentException("No resource found for: " + pathWithLines.path);
        }
        if (resultType.equals(CucumberFeature.class)) {
            FeatureBuilder builder = new FeatureBuilder((List<CucumberFeature>) cucumberFeatures);
            for (Resource resource : resources) {
                builder.parse(resource, filtersForPath);
            }
        } else {
            for (Resource resource : resources) {
                cucumberFeatures.add((T) resource);
            }
        }
        return cucumberFeatures;
    }

    private class CommonUriComparator<T> implements Comparator<T> {
        @Override
        public int compare(T a, T b) {
            if (a instanceof CucumberFeature && b instanceof CucumberFeature) {
                return ((CucumberFeature) a).getPath().compareTo(((CucumberFeature) b).getPath());
            } else {
                return ((Resource) a).getPath().compareTo(((Resource) b).getPath());
            }
        }
    }
}
