package io.cucumber.testng;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
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
import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.runtime.TypeRegistryConfigurerSupplier;
import org.apiguardian.api.API;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.cucumber.testng.TestCaseResultObserver.observe;
import static java.util.stream.Collectors.toList;

/**
 * Glue code for running Cucumber via TestNG.
 * <p>
 * Options can be provided in by (order of precedence):
 * <ol>
 * <li>Properties from {@link System#getProperties()}</li>
 * <li>Properties from in {@link System#getenv()}</li>
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
        // Parse the options early to provide fast feedback about invalid
        // options
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromPropertiesFile())
                .build();

        RuntimeOptions annotationOptions = new CucumberOptionsAnnotationParser()
                .withOptionsProvider(new TestNGCucumberOptionsProvider())
                .parse(clazz)
                .build(propertiesFileOptions);

        RuntimeOptions environmentOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromEnvironment())
                .build(annotationOptions);

        RuntimeOptions runtimeOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromSystemProperties())
                .enablePublishPlugin()
                .build(environmentOptions);

        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

        Supplier<ClassLoader> classLoader = ClassLoaders::getDefaultClassLoader;
        FeatureParser parser = new FeatureParser(bus::generateId);
        FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(classLoader, runtimeOptions,
            parser);

        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        ExitStatus exitStatus = new ExitStatus(runtimeOptions);
        plugins.addPlugin(exitStatus);
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
        ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader);
        BackendServiceLoader backendSupplier = new BackendServiceLoader(clazz::getClassLoader, objectFactorySupplier);
        this.filters = new Filters(runtimeOptions);
        TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(
            classLoader, runtimeOptions);
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier,
            objectFactorySupplier, typeRegistryConfigurerSupplier);
        this.context = new CucumberExecutionContext(bus, exitStatus, runnerSupplier);

        // Start test execution now.
        plugins.setSerialEventBusOnEventListenerPlugins(bus);
        features = featureSupplier.get();
        context.startTestRun();
        features.forEach(context::beforeFeature);
    }

    public void runScenario(io.cucumber.testng.Pickle pickle) {
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
        context.finishTestRun();
    }

    /**
     * @return returns the cucumber scenarios as a two dimensional array of
     *         {@link PickleWrapper} scenarios combined with their
     *         {@link FeatureWrapper} feature.
     */
    public Object[][] provideScenarios() {
        // Possibly invoked in a multi-threaded context
        try {
            return features.stream()
                    .flatMap(feature -> feature.getPickles().stream()
                            .filter(filters)
                            .map(cucumberPickle -> new Object[] {
                                    new PickleWrapperImpl(new io.cucumber.testng.Pickle(cucumberPickle)),
                                    new FeatureWrapperImpl(feature) }))
                    .collect(toList())
                    .toArray(new Object[0][0]);
        } catch (CucumberException e) {
            return new Object[][] { new Object[] { new CucumberExceptionWrapper(e), null } };
        }
    }

}
