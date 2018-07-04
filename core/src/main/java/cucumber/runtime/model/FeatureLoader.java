package cucumber.runtime.model;

import cucumber.runtime.CucumberException;
import cucumber.runtime.io.FileResource;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.util.Encoding;
import cucumber.util.FixJava;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FeatureLoader {
    private static final Pattern RERUN_PATH_SPECIFICATION = Pattern.compile("(?m:^| |)(.*?\\.feature(?:(?::\\d+)*))");

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
        for (String featurePath : featurePaths) {
            if (featurePath.startsWith("@")) {
                loadFromRerunFile(cucumberFeatures, resourceLoader, featurePath.substring(1));
            } else {
                loadFromFeaturePath(cucumberFeatures, resourceLoader, featurePath, false);
            }
        }
        Collections.sort(cucumberFeatures, new CucumberFeature.CucumberFeatureUriComparator());
        return cucumberFeatures;
    }


    private void loadFromRerunFile(List<CucumberFeature> cucumberFeatures, ResourceLoader resourceLoader, String rerunPath) {
        for (PathWithLines pathWithLines : loadRerunFile(rerunPath)) {
            loadFromFileSystemOrClasspath(cucumberFeatures, resourceLoader, pathWithLines.path);
        }
    }

    public List<PathWithLines> loadRerunFile(String rerunPath) {
        List<PathWithLines> featurePaths = new ArrayList<PathWithLines>();
        Iterable<Resource> resources = resourceLoader.resources(rerunPath, null);
        for (Resource resource : resources) {
            String source = read(resource);
            if (!source.isEmpty()) {
                Matcher matcher = RERUN_PATH_SPECIFICATION.matcher(source);
                while (matcher.find()) {
                    featurePaths.add(new PathWithLines(matcher.group(1)));
                }
            }
        }
        return featurePaths;
    }

    private static String read(Resource resource) {
        try {
            return Encoding.readFile(resource);
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }


    private static void loadFromFileSystemOrClasspath(List<CucumberFeature> cucumberFeatures, ResourceLoader resourceLoader, String featurePath) {
        try {
            loadFromFeaturePath(cucumberFeatures, resourceLoader, featurePath, false);
        } catch (IllegalArgumentException originalException) {
            if (!featurePath.startsWith(MultiLoader.CLASSPATH_SCHEME) &&
                originalException.getMessage().contains("Not a file or directory")) {
                try {
                    loadFromFeaturePath(cucumberFeatures, resourceLoader, MultiLoader.CLASSPATH_SCHEME + featurePath, true);
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

    private static void loadFromFeaturePath(List<CucumberFeature> cucumberFeatures, ResourceLoader resourceLoader, String featurePath, boolean failOnNoResource) {
        Iterable<Resource> resources = resourceLoader.resources(featurePath, ".feature");
        if (failOnNoResource && !resources.iterator().hasNext()) {
            throw new IllegalArgumentException("No resource found for: " + featurePath);
        }
        for (Resource resource : resources) {
            // TODO:
            // cucumberFeatures.add(CucumberFeature.fromFile(new File(featurePath)));
            try {
                // Hack for backwards compatibility. This will write the file to a temp file so Gherkin-Go can parse it.
                cucumberFeatures.add(CucumberFeature.fromSourceForTest(resource.getPath(), FixJava.readReader(new InputStreamReader(resource.getInputStream(), "UTF-8"))));
            } catch (IOException e) {
                throw new CucumberException(e);
            }
        }
    }
}
