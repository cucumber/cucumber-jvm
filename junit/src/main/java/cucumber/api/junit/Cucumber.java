package cucumber.api.junit;

import cucumber.api.CucumberOptions;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.api.formatter.Formatter;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.FeatureSupplier;
import cucumber.runtime.Filters;
import cucumber.runtime.RerunFilters;
import cucumber.runtime.model.FeatureLoader;
import cucumber.runtime.RunnerSupplier;
import cucumber.runtime.RuntimeGlueSupplier;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.Assertions;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitOptions;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Classes annotated with {@code @RunWith(Cucumber.class)} will run a Cucumber Feature.
 * In general, the runner class should be empty without any fields or methods.
 * For example:
 * <blockquote><pre>
 * &#64;RunWith(Cucumber.class)
 * &#64;CucumberOptions(plugin = "pretty")
 * public class RunCukesTest {
 * }
 * </pre></blockquote>
 * <p>
 * Cucumber will look for a {@code .feature} file on the classpath, using the same resource
 * path as the annotated class ({@code .class} substituted by {@code .feature}).
 * <p>
 * Additional hints can be given to Cucumber by annotating the class with {@link CucumberOptions}.
 * <p>
 * Cucumber also supports JUnits {@link ClassRule}, {@link BeforeClass} and {@link AfterClass} annotations.
 * These will be executed before and after all scenarios. Using these is not recommended as it limits the portability
 * between different runners; they may not execute correctly when using the commandline, IntelliJ IDEA or
 * Cucumber-Eclipse. Instead it is recommended to use Cucumbers `Before` and `After` hooks.
 *
 * @see CucumberOptions
 */
public class Cucumber extends ParentRunner<FeatureRunner> {
    private final JUnitReporter jUnitReporter;
    private final List<FeatureRunner> children = new ArrayList<FeatureRunner>();
    private final Runtime runtime;
    private final Formatter formatter;
    private final EventBus bus;

    /**
     * Constructor called by JUnit.
     *
     * @param clazz the class with the @RunWith annotation.
     * @throws org.junit.runners.model.InitializationError if there is another problem
     */
    public Cucumber(Class clazz) throws InitializationError {
        super(clazz);
        ClassLoader classLoader = clazz.getClassLoader();
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendSupplier backendSupplier = new BackendSupplier(resourceLoader, classFinder, runtimeOptions);
        bus = new EventBus(TimeService.SYSTEM);
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        FeatureSupplier featureSupplier = new FeatureSupplier(resourceLoader, runtimeOptions);
        RunnerSupplier runnerSupplier = new RunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        runtime = new Runtime(classLoader, runtimeOptions, bus, filters, runnerSupplier, featureSupplier);
        formatter = runtimeOptions.formatter(classLoader);
        final JUnitOptions junitOptions = new JUnitOptions(runtimeOptions.getJunitOptions());
        jUnitReporter = new JUnitReporter(bus, runtimeOptions.isStrict(), junitOptions);
        final StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);

        // Start the run before reading the features.
        // Allows the test source read events to be broadcast properly

        final List<CucumberFeature> features = featureLoader.load(runtimeOptions.getFeaturePaths(), System.out);
        runtimeOptions.getPlugins(); // to create the formatter objects
        bus.send(new TestRunStarted(bus.getTime()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }
        runtime.getRunner().reportStepDefinitions(stepDefinitionReporter);
        addChildren(features);
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
    protected Statement childrenInvoker(RunNotifier notifier) {
        final Statement features = super.childrenInvoker(notifier);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                features.evaluate();
                bus.send(new TestRunFinished(bus.getTime()));
            }
        };
    }

    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            FeatureRunner featureRunner = new FeatureRunner(cucumberFeature, runtime, jUnitReporter);
            if (!featureRunner.isEmpty()) {
                children.add(featureRunner);
            }
        }
    }
}
