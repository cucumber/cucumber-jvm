package cucumber.junit;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import cucumber.runtime.snippets.SummaryPrinter;
import gherkin.formatter.model.Feature;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Classes annotated with {@code @RunWith(Cucumber.class)} will run a Cucumber Feature.
 * The class should be empty without any fields or methods.
 * <p/>
 * Cucumber will look for a {@code .feature} file on the classpath, using the same resource
 * path as the annotated class ({@code .class} substituted by {@code .feature}).
 * <p/>
 * Additional hints can be given to Cucumber by annotating the class with {@link cucumber.junit.Feature}.
 *
 * @see cucumber.junit.Feature
 */
public class Cucumber extends Suite {
    private static final Runtime runtime = new Runtime();
    private static JUnitReporter jUnitReporter;

    static {
        jUnitReporter = JUnitReporterFactory.create(System.getProperty("cucumber.reporter"));
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                new SummaryPrinter(System.out).print(runtime);
            }
        });
    }

    private final Feature feature;
    private final String featurePath;

    /**
     * Constructor called by JUnit.
     */
    public Cucumber(Class featureClass) throws InitializationError, IOException {
        super(featureClass, new ArrayList<Runner>());

        featurePath = featurePath(featureClass);
        this.feature = parseFeature(featurePath, filters(featureClass));
    }

    private String featurePath(Class featureClass) {
        cucumber.junit.Feature featureAnnotation = (cucumber.junit.Feature) featureClass.getAnnotation(cucumber.junit.Feature.class);
        String pathName;
        if (featureAnnotation != null) {
            pathName = featureAnnotation.value();
        } else {
            pathName = featureClass.getName().replace('.', '/') + ".feature";
        }
        return pathName;
    }

    private List<Object> filters(Class featureClass) {
        cucumber.junit.Feature featureAnnotation = (cucumber.junit.Feature) featureClass.getAnnotation(cucumber.junit.Feature.class);
        Object[] filters = new Object[0];
        if (featureAnnotation != null) {
            filters = toLong(featureAnnotation.lines());
            if (filters.length == 0) {
                filters = featureAnnotation.tags();
            }
        }
        return asList(filters);
    }

    @Override
    public String getName() {
        return feature != null ? feature.getKeyword() + ": " + feature.getName() : "No matching features - did you use too restrictive filters?";
    }

    @Override
    public void run(RunNotifier notifier) {
        if (feature != null) {
            jUnitReporter.uri(featurePath);
            jUnitReporter.feature(feature);
            super.run(notifier);
            jUnitReporter.eof();
        }
    }

    private Feature parseFeature(String pathName, final List<Object> filters) {
        List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
        Resources.scan(pathName, new Consumer() {
            public void consume(Resource resource) {
                builder.parse(resource, filters);
            }
        });

        if (cucumberFeatures.isEmpty()) {
            return null;
        } else {
            CucumberFeature cucumberFeature = cucumberFeatures.get(0);
            buildFeatureElementRunners(cucumberFeature);
            return cucumberFeature.getFeature();
        }
    }

    private void buildFeatureElementRunners(CucumberFeature cucumberFeature) {
        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            try {
                List<String> gluePaths = gluePaths(super.getTestClass().getJavaClass());
                ParentRunner featureElementRunner;
                if (cucumberTagStatement instanceof CucumberScenario) {
                    featureElementRunner = new ExecutionUnitRunner(runtime, gluePaths, (CucumberScenario) cucumberTagStatement, jUnitReporter);
                } else {
                    featureElementRunner = new ScenarioOutlineRunner(runtime, gluePaths, (CucumberScenarioOutline) cucumberTagStatement, jUnitReporter);
                }
                getChildren().add(featureElementRunner);
            } catch (InitializationError e) {
                throw new RuntimeException("Failed to create scenario runner", e);
            }
        }
    }

    private Long[] toLong(long[] primitiveLongs) {
        Long[] longs = new Long[primitiveLongs.length];
        for (int i = 0; i < primitiveLongs.length; i++) {
            longs[i] = primitiveLongs[i];
        }
        return longs;
    }

    private List<String> gluePaths(Class featureClass) {
        List<String> gluePaths = new ArrayList<String>();
        String featurePackageName = featureClass.getName().substring(0, featureClass.getName().lastIndexOf("."));
        gluePaths.add(featurePackageName);

        // Add additional ones
        cucumber.junit.Feature featureAnnotation = (cucumber.junit.Feature) featureClass.getAnnotation(cucumber.junit.Feature.class);
        if (featureAnnotation != null) {
            gluePaths.addAll(asList(featureAnnotation.packages()));
        }
        return gluePaths;
    }
}
