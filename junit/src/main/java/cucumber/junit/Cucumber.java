package cucumber.junit;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.snippets.SummaryPrinter;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cucumber.runtime.Utils.packagePath;
import static cucumber.runtime.model.CucumberFeature.loadFromClasspath;
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
public class Cucumber extends ParentRunner<FeatureRunner> {
    private final JUnitReporter jUnitReporter;
    private final List<FeatureRunner> children = new ArrayList<FeatureRunner>();
    private final Runtime runtime;

    /**
     * Constructor called by JUnit.
     *
     * @param clazz the class with the @RunWith annotation.
     * @throws java.io.IOException if there is a problem
     * @throws org.junit.runners.model.InitializationError
     *                             if there is another problem
     */
    public Cucumber(Class clazz) throws InitializationError, IOException {
        super(clazz);
        assertNoDeclaredMethods(clazz);
        List<String> featurePaths = featurePaths(clazz);

        List<String> gluePaths = gluePaths(clazz);
        runtime = new Runtime(gluePaths);

        jUnitReporter = JUnitReporterFactory.create(System.getProperty("cucumber.reporter"));
        addChildren(featurePaths, filters(clazz), gluePaths(clazz));
    }

    @Override
    protected List<FeatureRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(FeatureRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(FeatureRunner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        jUnitReporter.done();
        new SummaryPrinter(System.out).print(runtime);
    }

    private void assertNoDeclaredMethods(Class clazz) {
        if (clazz.getDeclaredMethods().length != 0) {
            throw new CucumberException(
                    "\n\n" +
                            "Classes annotated with @RunWith(Cucumber.class) must not define any methods.\n" +
                            "Their sole purpose is to serve as an entry point for JUnit.\n" +
                            "Step Definitions should be defined in their own classes.\n" +
                            "This allows them to be reused across features.\n" +
                            "Offending class: " + clazz + "\n"
            );
        }
    }

    /**
     * @param clazz the Class used to kick it all off
     * @return either a path to a single feature, or to a directory or classpath entry containing them
     */
    private List<String> featurePaths(Class clazz) {
        cucumber.junit.Feature featureAnnotation = getFeatureAnnotation(clazz);
        String featurePath;
        if (featureAnnotation != null) {
            featurePath = featureAnnotation.value();
        } else {
            featurePath = packagePath(clazz);
        }
        return asList(featurePath);
    }

    private List<String> gluePaths(Class clazz) {
        List<String> gluePaths = new ArrayList<String>();

        gluePaths.add(packagePath(clazz));

        // Add additional ones
        cucumber.junit.Feature featureAnnotation = getFeatureAnnotation(clazz);
        if (featureAnnotation != null) {
            for (String packageName : featureAnnotation.packages()) {
                gluePaths.add(packagePath(packageName));
            }
        }
        return gluePaths;
    }

    private List<Object> filters(Class clazz) {
        cucumber.junit.Feature featureAnnotation = getFeatureAnnotation(clazz);
        Object[] filters = new Object[0];
        if (featureAnnotation != null) {
            filters = toLong(featureAnnotation.lines());
            if (filters.length == 0) {
                filters = featureAnnotation.tags();
            }
        }
        return asList(filters);
    }

    private void addChildren(List<String> featurePaths, final List<Object> filters, List<String> gluePaths) throws InitializationError {
        List<CucumberFeature> cucumberFeatures = loadFromClasspath(featurePaths, filters);
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            children.add(new FeatureRunner(cucumberFeature, gluePaths, runtime, jUnitReporter));
        }
    }

    private Long[] toLong(long[] primitiveLongs) {
        Long[] longs = new Long[primitiveLongs.length];
        for (int i = 0; i < primitiveLongs.length; i++) {
            longs[i] = primitiveLongs[i];
        }
        return longs;
    }

    private Feature getFeatureAnnotation(Class clazz) {
        return (Feature) clazz.getAnnotation(Feature.class);
    }
}
