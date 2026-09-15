package io.cucumber.testng;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.options.Constants;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.CucumberExecutionContext;
import io.cucumber.core.runtime.ExitStatus;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.runtime.UuidGeneratorServiceLoader;
import org.apiguardian.api.API;

import java.time.Clock;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.cucumber.core.runtime.SynchronizedEventBus.synchronize;
import static io.cucumber.testng.TestCaseResultObserver.observe;

/**
 * Glue code for running Cucumber via TestNG.
 * <p>
 * Options can be provided in by (order of precedence):
 * <ol>
 * <li>Properties from {@link System#getProperties()}</li>
 * <li>Properties from in {@link System#getenv()}</li>
 * <li>Properties properties from {@code testng.xml}</li>
 * <li>Annotating the runner class with {@link CucumberOptions}</li>
 * <li>Properties from {@value Constants#CUCUMBER_PROPERTIES_FILE_NAME}</li>
 * </ol>
 * For available properties see {@link Constants}.
 */
@API(status = API.Status.STABLE)
public final class TestNGCucumberRunner {

    private final Predicate<Pickle> filters;
    private final List<Feature> features;
    private final CucumberExecutionContext context;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the {@link CucumberOptions} and
     *              {@link org.testng.annotations.Test} annotations
     */
    public TestNGCucumberRunner(Class<?> clazz) {
        this(clazz, key -> null);
    }

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz      Which has the {@link CucumberOptions} and
     *                   {@link org.testng.annotations.Test} annotations
     * @param properties additional properties (e.g. from {@code testng.xml}).
     */
    @API(status = API.Status.STABLE, since = "6.11")
    public TestNGCucumberRunner(Class<?> clazz, CucumberPropertiesProvider properties) {
        // Parse the options early to provide fast feedback about invalid
        // options
        RuntimeOptions runtimeOptions = createRuntimeOptions(clazz, properties);
        Supplier<ClassLoader> classLoader = ClassLoaders::getDefaultClassLoader;
        UuidGenerator idGenerator = createIdGenerator(classLoader, runtimeOptions);
        FeaturePathFeatureSupplier featureSupplier = createFeatureSupplier(idGenerator, classLoader, runtimeOptions);

        this.filters = new Filters(runtimeOptions);
        this.features = featureSupplier.get();

        // Start test execution now.
        EventBus bus = synchronize(new TimeServiceEventBus(Clock.systemUTC(), idGenerator));
        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        ExitStatus exitStatus = new ExitStatus(runtimeOptions);
        plugins.addPlugin(exitStatus);
        plugins.setSerialEventBusOnEventListenerPlugins(bus);
        context = createExecutionContext(clazz, classLoader, runtimeOptions, bus, exitStatus);
        context.startTestRun();
        context.runBeforeAllHooks();
        features.forEach(context::beforeFeature);
    }

    private static RuntimeOptions createRuntimeOptions(Class<?> clazz, CucumberPropertiesProvider properties) {
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromPropertiesFile())
                .build();

        RuntimeOptions annotationOptions = new CucumberOptionsAnnotationParser()
                .withOptionsProvider(new TestNGCucumberOptionsProvider())
                .parse(clazz)
                .build(propertiesFileOptions);

        RuntimeOptions testngPropertiesOptions = new CucumberPropertiesParser()
                .parse(properties::get)
                .build(annotationOptions);

        RuntimeOptions environmentOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromEnvironment())
                .build(testngPropertiesOptions);

        return new CucumberPropertiesParser()
                .parse(CucumberProperties.fromSystemProperties())
                .enablePublishPlugin()
                .build(environmentOptions);
    }

    private static FeaturePathFeatureSupplier createFeatureSupplier(
            UuidGenerator idGenerator, Supplier<ClassLoader> classLoader, RuntimeOptions runtimeOptions
    ) {
        FeatureParser parser = new FeatureParser(idGenerator::generateId);
        return new FeaturePathFeatureSupplier(classLoader, runtimeOptions, parser);
    }

    private static UuidGenerator createIdGenerator(Supplier<ClassLoader> classLoader, RuntimeOptions runtimeOptions) {
        UuidGeneratorServiceLoader uuidGeneratorServiceLoader = new UuidGeneratorServiceLoader(classLoader,
            runtimeOptions);
        return uuidGeneratorServiceLoader.loadUuidGenerator();
    }

    private CucumberExecutionContext createExecutionContext(
            Class<?> clazz, Supplier<ClassLoader> classLoader, RuntimeOptions runtimeOptions, EventBus bus,
            ExitStatus exitStatus
    ) {
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(classLoader,
            runtimeOptions);
        ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader);
        BackendServiceLoader backendSupplier = new BackendServiceLoader(clazz::getClassLoader, objectFactorySupplier);
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier,
            objectFactorySupplier);
        return new CucumberExecutionContext(bus, exitStatus, runnerSupplier);
    }

    public void runScenario(io.cucumber.testng.Pickle pickle) {
        if (pickle.isDryRun()) {
            throw new IllegalArgumentException(
                "Pickle [%s] created by TestNGCucumberRunner.provideDryRunScenarios should not be executed"
                        .formatted(pickle.getName()));
        }

        context.runTestCase(runner -> {
            try (TestCaseResultObserver observer = observe(runner.getBus())) {
                Pickle cucumberPickle = pickle.getPickle();
                runner.runPickle(cucumberPickle);
                observer.assertTestCasePassed();
            }
        });
    }

    /**
     * Finishes test execution by Cucumber.
     */
    public void finish() {
        try {
            context.runAfterAllHooks();
        } finally {
            context.finishTestRun();
        }
    }

    /**
     * Provides scenarios for execution.
     *
     * @return an array of pickle and feature wrapper pairs.
     */
    public Object[][] provideScenarios() {
        // Possibly invoked in a multithreaded context
        return provideScenarios(features, filters, false);
    }

    /**
     * Provides scenarios for executing without initializing Cucumber. These
     * scenarios can only be used when TestNG does a dry-run.
     * <p>
     * For consistent results between dry-run and regular execution, this method
     * should be used when the {@linkplain TestNGCucumberRunner} is instantiated
     * by {@link TestNGCucumberRunner(Class)}.
     *
     * @param  clazz Which has the {@link CucumberOptions} and
     *               {@link org.testng.annotations.Test} annotations
     * @return       an array of pickle and feature wrapper pairs.
     */
    @API(status = API.Status.EXPERIMENTAL, since = "8.0.0")
    public static Object[][] provideDryRunScenarios(Class<?> clazz) {
        return provideDryRunScenarios(clazz, key -> null);
    }

    /**
     * Provides scenarios without initializing Cucumber. These scenarios can
     * only be used when TestNG does a dry-run. p> For consistent results
     * between dry-run and regular execution, this method should be used when
     * the {@linkplain TestNGCucumberRunner} is instantiated by
     * {@link TestNGCucumberRunner(Class, CucumberPropertiesProvider)}.
     *
     * @param  clazz Which has the {@link CucumberOptions} and
     *               {@link org.testng.annotations.Test} annotations
     * @return       an array of pickle and feature wrapper pairs.
     */
    @API(status = API.Status.EXPERIMENTAL, since = "8.0.0")
    public static Object[][] provideDryRunScenarios(Class<?> clazz, CucumberPropertiesProvider properties) {
        RuntimeOptions runtimeOptions = createRuntimeOptions(clazz, properties);
        Supplier<ClassLoader> classLoader = ClassLoaders::getDefaultClassLoader;
        UuidGenerator idGenerator = createIdGenerator(classLoader, runtimeOptions);
        FeaturePathFeatureSupplier featureSupplier = createFeatureSupplier(idGenerator, classLoader, runtimeOptions);

        Filters filters = new Filters(runtimeOptions);
        List<Feature> features = featureSupplier.get();
        return provideScenarios(features, filters, true);
    }

    private static Object[][] provideScenarios(List<Feature> features, Predicate<Pickle> filters, boolean dryrun) {
        return features.stream()
                .flatMap(feature -> feature.getPickles().stream()
                        .filter(filters)
                        .map(cucumberPickle -> new Object[] {

                                new PickleWrapperImpl(new io.cucumber.testng.Pickle(cucumberPickle, dryrun)),
                                new FeatureWrapperImpl(feature) }))
                .toArray(size -> new Object[size][2]);
    }

}
