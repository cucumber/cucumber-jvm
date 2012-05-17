package cucumber.junit;

import cucumber.io.MultiLoader;
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
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
        assertNoCucumberAnnotatedMethods(clazz);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        runtime = new Runtime(resourceLoader, classLoader, runtimeOptions);

        jUnitReporter = new JUnitReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.strict);
        addChildren(runtimeOptions.cucumberFeatures(resourceLoader));
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

    static void assertNoCucumberAnnotatedMethods(Class clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().getName().startsWith("cucumber")) {
                    throw new CucumberException(
                            "\n\n" +
                                    "Classes annotated with @RunWith(Cucumber.class) must not define any\n" +
                                    "Step Definition or Hook methods. Their sole purpose is to serve as\n" +
                                    "an entry point for JUnit. Step Definitions and Hooks should be defined\n" +
                                    "in their own classes. This allows them to be reused across features.\n" +
                                    "Offending class: " + clazz + "\n"
                    );
                }
            }
        }
    }

    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            children.add(new FeatureRunner(cucumberFeature, runtime, jUnitReporter));
        }
    }

    /**
     * This annotation can be used to give additional hints to the {@link cucumber.junit.Cucumber} runner
     * about what to run. It provides similar options to the Cucumber command line used by {@link cucumber.cli.Main}
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public static @interface Options {
        /**
         * @return true if this is a dry run
         */
        boolean dryRun() default false;

        /**
         * @return true if strict mode is enabled (fail if there are undefined or pending steps)
         */
        boolean strict() default false;

        /**
         * @return the paths to the feature(s)
         */
        String[] features() default {};

        /**
         * @return where to look for glue code (stepdefs and hooks)
         */
        String[] glue() default {};

        /**
         * @return what tags in the features should be executed
         */
        String[] tags() default {};

        /**
         * @return what formatter(s) to use
         */
        String[] format() default {};

        /**
         * @return whether or not to use monochrome output
         */
        boolean monochrome() default false;

    }
}