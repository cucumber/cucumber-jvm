package cucumber.junit;

import cucumber.formatter.NullReporter;
import cucumber.io.ClasspathResourceLoader;
import cucumber.io.ResourceLoader;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.snippets.SummaryPrinter;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static cucumber.runtime.Utils.packagePath;
import static cucumber.runtime.model.CucumberFeature.load;
import static java.util.Arrays.asList;

/**
 * Classes annotated with {@code @RunWith(Cucumber.class)} will run a Cucumber Feature.
 * The class should be empty without any fields or methods.
 * <p/>
 * Cucumber will look for a {@code .feature} file on the classpath, using the same resource
 * path as the annotated class ({@code .class} substituted by {@code .feature}).
 * <p/>
 * Additional hints can be given to Cucumber by annotating the class with {@link Options}.
 *
 * @see Options
 */
public class Cucumber extends ParentRunner<FeatureRunner> {
    private final ResourceLoader resourceLoader;
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
        ClassLoader classLoader = clazz.getClassLoader();
        resourceLoader = new ClasspathResourceLoader(classLoader);
        assertNoDeclaredMethods(clazz);
        List<String> featurePaths = featurePaths(clazz);
        List<String> gluePaths = gluePaths(clazz);
        RuntimeOptions runtimeOptions = runtimeOptions(clazz);
        runtime = new Runtime(resourceLoader, gluePaths, classLoader, runtimeOptions);

        // TODO: Create formatter(s) based on Annotations. Use same technique as in cli.Main for MultiFormatter
        jUnitReporter = new JUnitReporter(new NullReporter(), new NullReporter());
        addChildren(featurePaths, filters(clazz));
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
        jUnitReporter.close();
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

    private RuntimeOptions runtimeOptions(Class clazz) {
        Options cucumberOptions = getFeatureAnnotation(clazz);
        
        List<String> args = new ArrayList<String>();
        
        RuntimeOptions runtimeOptions = new RuntimeOptions(args.toArray(new String[args.size()]));
        return runtimeOptions;
    }

    /**
     * @param clazz the Class used to kick it all off
     * @return either a path to a single feature, or to a directory or classpath entry containing them
     */
    private List<String> featurePaths(Class clazz) {
        Options cucumberOptions = getFeatureAnnotation(clazz);
        String featurePath;
        if (cucumberOptions != null) {
            featurePath = cucumberOptions.value();
        } else {
            featurePath = packagePath(clazz);
        }
        return asList(featurePath);
    }

    private List<String> gluePaths(Class clazz) {
        List<String> gluePaths = new ArrayList<String>();

        gluePaths.add(packagePath(clazz));

        // Add additional ones
        Options cucumberOptions = getFeatureAnnotation(clazz);
        if (cucumberOptions != null) {
            for (String packageName : cucumberOptions.packages()) {
                gluePaths.add(packagePath(packageName));
            }
        }
        return gluePaths;
    }

    private List<Object> filters(Class clazz) {
        Options cucumberOptions = getFeatureAnnotation(clazz);
        Object[] filters = new Object[0];
        if (cucumberOptions != null) {
            filters = toLong(cucumberOptions.lines());
            if (filters.length == 0) {
                filters = cucumberOptions.tags();
            }
        }
        return asList(filters);
    }

    private void addChildren(List<String> featurePaths, final List<Object> filters) throws InitializationError {
        List<CucumberFeature> cucumberFeatures = load(resourceLoader, featurePaths, filters);
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            children.add(new FeatureRunner(cucumberFeature, runtime, jUnitReporter));
        }
    }

    private Long[] toLong(long[] primitiveLongs) {
        Long[] longs = new Long[primitiveLongs.length];
        for (int i = 0; i < primitiveLongs.length; i++) {
            longs[i] = primitiveLongs[i];
        }
        return longs;
    }

    private Options getFeatureAnnotation(Class clazz) {
        return (Options) clazz.getAnnotation(Options.class);
    }

    /**
     * This annotation can be used to give additional hints to the {@link cucumber.junit.Cucumber} runner
     * about what to run. It provides similar options to the Cucumber command line used by {@link cucumber.cli.Main}
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public static @interface Options {
        /**
         * @return the path to the feature(s)
         */
        String value();

        /**
         * @return what lines in the feature should be executed
         */
        long[] lines() default {};

        /**
         * @return what tags in the feature should be executed
         */
        String[] tags() default {};

        /**
         * @return where to look for glue code (stepdefs and hooks)
         */
        String[] packages() default {};
    }
}
