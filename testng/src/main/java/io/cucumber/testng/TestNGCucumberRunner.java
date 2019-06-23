package io.cucumber.testng;

import gherkin.events.PickleEvent;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.event.TestRunStarted;
import io.cucumber.core.api.event.TestSourceRead;
import io.cucumber.core.runtime.ConfiguringTypeRegistrySupplier;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.FeatureLoader;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.Env;
import io.cucumber.core.options.EnvironmentOptionsParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runner.TimeServiceEventBus;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TypeRegistrySupplier;
import org.apiguardian.api.API;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Glue code for running Cucumber via TestNG.
 */
@API(status = API.Status.STABLE)
public final class TestNGCucumberRunner {
    private final EventBus bus;
    private final Filters filters;
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final RuntimeOptions runtimeOptions;
    private final Plugins plugins;
    private final FeaturePathFeatureSupplier featureSupplier;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the {@link CucumberOptions}
     *              and {@link org.testng.annotations.Test} annotations
     */
    public TestNGCucumberRunner(Class clazz) {

        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);

        // Parse the options early to provide fast feedback about invalid options
        RuntimeOptions annotationOptions = new CucumberOptionsAnnotationParser(resourceLoader)
            .withOptionsProvider(new TestNGCucumberOptionsProvider())
            .parse(clazz)
            .build();
        runtimeOptions = new EnvironmentOptionsParser(resourceLoader)
            .parse(Env.INSTANCE)
            .build(annotationOptions);

        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);

        this.bus = new TimeServiceEventBus(Clock.systemUTC());
        this.plugins = new Plugins(new PluginFactory(), runtimeOptions);
        ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier();
        BackendServiceLoader backendSupplier = new BackendServiceLoader(resourceLoader, objectFactorySupplier);
        this.filters = new Filters(runtimeOptions);
        TypeRegistrySupplier typeRegistrySupplier = new ConfiguringTypeRegistrySupplier(classFinder, runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, objectFactorySupplier, typeRegistrySupplier);

    }

    public void runScenario(PickleEvent pickle) throws Throwable {
        //Possibly invoked in a multi-threaded context
        Runner runner = runnerSupplier.get();
        TestCaseResultListener testCaseResultListener = new TestCaseResultListener(runner.getBus(), runtimeOptions.isStrict());
        runner.runPickle(pickle);
        testCaseResultListener.finishExecutionUnit();

        if (!testCaseResultListener.isPassed()) {
            throw testCaseResultListener.getError();
        }
    }

    public void finish() {
        bus.send(new TestRunFinished(bus.getInstant()));
    }

    /**
     * @return returns the cucumber scenarios as a two dimensional array of {@link PickleEventWrapper}
     * scenarios combined with their {@link CucumberFeatureWrapper} feature.
     */
    public Object[][] provideScenarios() {
        try {
            List<Object[]> scenarios = new ArrayList<Object[]>();
            List<CucumberFeature> features = getFeatures();
            for (CucumberFeature feature : features) {
                for (PickleEvent pickle : feature.getPickles()) {
                    if (filters.matchesFilters(pickle)) {
                        scenarios.add(new Object[]{new PickleEventWrapperImpl(pickle),
                            new CucumberFeatureWrapperImpl(feature)});
                    }
                }
            }
            return scenarios.toArray(new Object[][]{});
        } catch (CucumberException e) {
            return new Object[][]{new Object[]{new CucumberExceptionWrapper(e), null}};
        }
    }

    List<CucumberFeature> getFeatures() {
        plugins.setSerialEventBusOnEventListenerPlugins(bus);

        List<CucumberFeature> features = featureSupplier.get();
        bus.send(new TestRunStarted(bus.getInstant()));
        for (CucumberFeature feature : features) {
            bus.send(new TestSourceRead(bus.getInstant(), feature.getUri().toString(), feature.getSource()));
        }
        return features;
    }
}
