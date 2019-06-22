package io.cucumber.junit;

import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.BackendModuleBackendSupplier;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Env;
import io.cucumber.core.options.EnvironmentOptionsParser;
import cucumber.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import org.apiguardian.api.API;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
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
 * public class RunCucumberTest {
 * }
 * </pre></blockquote>
 * <p>
 * By default Cucumber will look for {@code .feature} and glue files on the classpath, using the same resource
 * path as the annotated class. For example, if the annotated class is {@code com.example.RunCucumber} then
 * features and glue are assumed to be located in {@code com.example}.
 * <p>
 * Additional hints can be provided to Cucumber by annotating the class with {@link CucumberOptions}.
 * <p>
 * Cucumber also supports JUnits {@link ClassRule}, {@link BeforeClass} and {@link AfterClass} annotations.
 * These will be executed before and after all scenarios. Using these is not recommended as it limits the portability
 * between different runners; they may not execute correctly when using the commandline, IntelliJ IDEA or
 * Cucumber-Eclipse. Instead it is recommended to use Cucumbers `Before` and `After` hooks.
 *
 * @see CucumberOptions
 */
@API(status = API.Status.STABLE)
public class Cucumber extends ParentRunner<FeatureRunner> {
    private final List<FeatureRunner> children = new ArrayList<>();
    private final EventBus bus;
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final List<CucumberFeature> features;
    private final Plugins plugins;

    private boolean multiThreadingAssumed = false;

    /**
     * Constructor called by JUnit.
     *
     * @param clazz the class with the @RunWith annotation.
     * @throws org.junit.runners.model.InitializationError if there is another problem
     */
    public Cucumber(Class clazz) throws InitializationError {
        super(clazz);
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        // Parse the options early to provide fast feedback about invalid options
        RuntimeOptions annotationOptions = new CucumberOptionsAnnotationParser(resourceLoader)
            .withOptionsProvider(new JUnitCucumberOptionsProvider())
            .parse(clazz)
            .build();

        RuntimeOptions runtimeOptions = new EnvironmentOptionsParser(resourceLoader)
            .parse(Env.INSTANCE)
            .build(annotationOptions);

        JUnitOptions junitAnnotationOptions = new JUnitOptionsParser()
            .parse(clazz)
            .build();

        JUnitOptions junitOptions = new JUnitOptionsParser()
            .parse(runtimeOptions.getJunitOptions())
            .setStrict(runtimeOptions.isStrict())
            .build(junitAnnotationOptions);


        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);

        // Parse the features early. Don't proceed when there are lexer errors
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);
        this.features = featureSupplier.get();

        // Create plugins after feature parsing to avoid the creation of empty files on lexer errors.
        this.plugins = new Plugins(classLoader, new PluginFactory(), runtimeOptions);
        this.bus = new TimeServiceEventBus(TimeService.SYSTEM);

        BackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier);
        Filters filters = new Filters(runtimeOptions);
        for (CucumberFeature cucumberFeature : features) {
            FeatureRunner featureRunner = new FeatureRunner(cucumberFeature, filters, runnerSupplier, junitOptions);
            if (!featureRunner.isEmpty()) {
                children.add(featureRunner);
            }
        }
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
        Statement runFeatures = super.childrenInvoker(notifier);
        return new RunCucumber(runFeatures);
    }

    class RunCucumber extends Statement {
        private final Statement runFeatures;

        RunCucumber(Statement runFeatures) {
            this.runFeatures = runFeatures;
        }

        @Override
        public void evaluate() throws Throwable {
            if (multiThreadingAssumed) {
                plugins.setSerialEventBusOnEventListenerPlugins(bus);
            } else {
                plugins.setEventBusOnEventListenerPlugins(bus);
            }

            bus.send(new TestRunStarted(bus.getTime(), bus.getTimeMillis()));
            for (CucumberFeature feature : features) {
                feature.sendTestSourceRead(bus);
            }
            StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
            runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);
            runFeatures.evaluate();
            bus.send(new TestRunFinished(bus.getTime(), bus.getTimeMillis()));
        }
    }

    @Override
    public void setScheduler(RunnerScheduler scheduler) {
        super.setScheduler(scheduler);
        multiThreadingAssumed = true;
    }
}
