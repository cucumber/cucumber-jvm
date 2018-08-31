package cucumber.api.junit;

import cucumber.api.CucumberOptions;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import io.cucumber.junit.Assertions;
import io.cucumber.junit.FeatureRunner;
import io.cucumber.junit.JUnitOptions;
import io.cucumber.core.runner.EventBus;
import io.cucumber.core.runner.TimeService;
import io.cucumber.core.runtime.BackendModuleBackendSupplier;
import io.cucumber.core.backend.BackendSupplier;
import io.cucumber.core.runner.TimeServiceEventBus;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.filter.RerunFilters;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.model.FeatureLoader;
import io.cucumber.core.runner.ThreadLocalRunnerSupplier;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsFactory;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.model.CucumberFeature;
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
 * Fail * </pre></blockquote>
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
    private final List<FeatureRunner> children = new ArrayList<FeatureRunner>();
    private final EventBus bus;
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final Filters filters;
    private final JUnitOptions junitOptions;

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

        this.bus = new TimeServiceEventBus(TimeService.SYSTEM);

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions, bus);

        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        this.filters = new Filters(runtimeOptions, rerunFilters);
        this.junitOptions = new JUnitOptions(runtimeOptions.isStrict(), runtimeOptions.getJunitOptions());
        final StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();

        // Start the run before reading the features.
        // Allows the test source read events to be broadcast properly
        bus.send(new TestRunStarted(bus.getTime()));
        addChildren(featureSupplier.get());
        runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);
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
            FeatureRunner featureRunner = new FeatureRunner(cucumberFeature, filters, runnerSupplier, junitOptions);
            if (!featureRunner.isEmpty()) {
                children.add(featureRunner);
            }
        }
    }
}
