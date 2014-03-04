package cucumber.api.junit;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.Assertions;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
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
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz, new Class[]{CucumberOptions.class, Options.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);

        jUnitReporter = new JUnitReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.isStrict());
        addChildren(runtimeOptions.cucumberFeatures(resourceLoader));
    }

    @Override
    public List<FeatureRunner> getChildren() {
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
        runtime.runBeforeAllHooks(jUnitReporter);
        super.run(notifier);
        runtime.runAfterAllHooks(jUnitReporter);
        jUnitReporter.done();
        jUnitReporter.close();
        runtime.printSummary();
    }

    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            children.add(new FeatureRunner(cucumberFeature, runtime, jUnitReporter));
        }
    }

    // TODO: When Options is removed we should remove reflection from RuntimeOptionsFactory.

    /**
     * This annotation can be used to give additional hints to the {@link Cucumber} runner
     * about what to run. It provides similar options to the Cucumber command line used by {@link cucumber.api.cli.Main}
     *
     * @deprecated use {@link cucumber.api.CucumberOptions} instead.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Deprecated
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

        /**
         * Specify a patternfilter for features or scenarios
         *
         * @return a list of patterns
         */
        String[] name() default {};

        String dotcucumber() default "";

        /**
         * @return what format should the snippets use. underscore, camelcase
         */
        SnippetType snippets() default SnippetType.UNDERSCORE;
    }
}
