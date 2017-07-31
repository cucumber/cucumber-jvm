package cucumber.runtime.model;

import cucumber.api.event.TestSourceRead;
import cucumber.runner.EventBus;
import cucumber.runtime.CucumberException;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.util.Encoding;
import gherkin.ast.GherkinDocument;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CucumberFeature implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String uri;
    private GherkinDocument gherkinDocument;
    private String gherkinSource;
    public static final Pattern RERUN_PATH_SPECIFICATION = Pattern.compile("(?m:^| |)(.*?\\.feature(?:(?::\\d+)*))");

    public static List<CucumberFeature> load(ResourceLoader resourceLoader, List<String> featurePaths, PrintStream out) {
        final List<CucumberFeature> cucumberFeatures = load(resourceLoader, featurePaths);
        if (cucumberFeatures.isEmpty()) {
            if (featurePaths.isEmpty()) {
                out.println("Got no path to feature directory or feature file");
            } else {
                out.println(String.format("No features found at %s", featurePaths));
            }
        }
        return cucumberFeatures;
    }

    public static List<CucumberFeature> load(ResourceLoader resourceLoader, List<String> featurePaths) {
        final List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
        for (String featurePath : featurePaths) {
            if (featurePath.startsWith("@")) {
                loadFromRerunFile(builder, resourceLoader, featurePath.substring(1));
            } else {
                loadFromFeaturePath(builder, resourceLoader, featurePath, false);
            }
        }
        Collections.sort(cucumberFeatures, new CucumberFeatureUriComparator());
        return cucumberFeatures;
    }

    private static void loadFromRerunFile(FeatureBuilder builder, ResourceLoader resourceLoader, String rerunPath) {
        for(PathWithLines pathWithLines : loadRerunFile(resourceLoader, rerunPath)){
            loadFromFileSystemOrClasspath(builder, resourceLoader, pathWithLines.path);
        }
    }

    public static List<PathWithLines> loadRerunFile(ResourceLoader resourceLoader, String rerunPath) {
        List<PathWithLines> featurePaths = new ArrayList<PathWithLines>();
        Iterable<Resource> resources = resourceLoader.resources(rerunPath, null);
        for (Resource resource : resources) {
            String source = read(resource);
            if (!source.isEmpty()) {
                Matcher matcher = RERUN_PATH_SPECIFICATION.matcher(source);
                while(matcher.find()){
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

    public CucumberFeature(GherkinDocument gherkinDocument, String uri, String gherkinSource) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
    }

    public GherkinDocument getGherkinFeature() {
        return gherkinDocument;
    }

    public String getUri() {
        return uri;
    }

    public void sendTestSourceRead(EventBus bus) {
        bus.send(new TestSourceRead(bus.getTime(), uri, gherkinSource));
    }

    private static class CucumberFeatureUriComparator implements Comparator<CucumberFeature> {
        @Override
        public int compare(CucumberFeature a, CucumberFeature b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
