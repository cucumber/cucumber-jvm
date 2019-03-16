package io.cucumber.testng.api;

import gherkin.events.PickleEvent;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.event.TestRunStarted;
import io.cucumber.core.api.event.TestSourceRead;
import io.cucumber.core.api.plugin.StepDefinitionReporter;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.ObjectFactorySupplier;
import io.cucumber.core.backend.SingletonObjectFactorySupplier;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.model.CucumberFeature;
import io.cucumber.core.model.FeatureLoader;
import io.cucumber.core.options.Env;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsFactory;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runner.TimeService;
import io.cucumber.core.runner.TimeServiceEventBus;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;

import java.util.ArrayList;
import java.util.List;

import static io.cucumber.core.backend.ObjectFactoryLoader.loadObjectFactory;

/**
 * Glue code for running Cucumber via TestNG.
 */
public class TestNGCucumberRunner {
    private final EventBus bus;
    private final Filters filters;
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final RuntimeOptions runtimeOptions;
    private final Plugins plugins;
    private final FeaturePathFeatureSupplier featureSupplier;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the {@link io.cucumber.core.api.options.CucumberOptions}
     *              and {@link org.testng.annotations.Test} annotations
     */
    public TestNGCucumberRunner(Class clazz) {

        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);

        // Parse the options early to provide fast feedback about invalid options
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz, resourceLoader);
        runtimeOptions = runtimeOptionsFactory.create();

        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);

        this.bus = new TimeServiceEventBus(TimeService.SYSTEM);
        this.plugins = new Plugins(new PluginFactory(), bus, runtimeOptions);
        ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier();
        BackendServiceLoader backendSupplier = new BackendServiceLoader(resourceLoader, classFinder, runtimeOptions, objectFactory);
        this.filters = new Filters(runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, objectFactory);

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
        bus.send(new TestRunFinished(bus.getTime()));
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
        List<CucumberFeature> features = featureSupplier.get();

        bus.send(new TestRunStarted(bus.getTime()));
        for (CucumberFeature feature : features) {
            bus.send(new TestSourceRead(bus.getTime(), feature.getUri().toString(), feature.getSource()));
        }
        StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
        runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);
        return features;
    }
}
