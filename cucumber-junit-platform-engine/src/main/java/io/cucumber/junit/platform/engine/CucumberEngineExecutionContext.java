package io.cucumber.junit.platform.engine;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.CucumberExecutionContext;
import io.cucumber.core.runtime.ExitStatus;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.runtime.SingletonObjectFactorySupplier;
import io.cucumber.core.runtime.SingletonRunnerSupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.apiguardian.api.API;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

import static io.cucumber.core.runtime.SynchronizedEventBus.synchronize;
import static io.cucumber.junit.platform.engine.TestCaseResultObserver.observe;

@API(status = API.Status.STABLE)
public final class CucumberEngineExecutionContext implements EngineExecutionContext {

    private static final Logger log = LoggerFactory.getLogger(CucumberEngineExecutionContext.class);
    private final CucumberEngineOptions options;

    private CucumberExecutionContext context;

    CucumberEngineExecutionContext(ConfigurationParameters configurationParameters) {
        options = new CucumberEngineOptions(configurationParameters);
    }

    CucumberEngineOptions getOptions() {
        return options;
    }

    private CucumberExecutionContext createCucumberExecutionContext() {
        Supplier<ClassLoader> classLoader = CucumberEngineExecutionContext.class::getClassLoader;
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(classLoader, options);
        EventBus bus = synchronize(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));
        Plugins plugins = new Plugins(new PluginFactory(), options);
        ExitStatus exitStatus = new ExitStatus(options);
        plugins.addPlugin(exitStatus);

        RunnerSupplier runnerSupplier;
        if (options.isParallelExecutionEnabled()) {
            plugins.setSerialEventBusOnEventListenerPlugins(bus);
            ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(
                objectFactoryServiceLoader);
            BackendSupplier backendSupplier = new BackendServiceLoader(classLoader, objectFactorySupplier);
            runnerSupplier = new ThreadLocalRunnerSupplier(options, bus, backendSupplier, objectFactorySupplier);
        } else {
            plugins.setEventBusOnEventListenerPlugins(bus);
            ObjectFactorySupplier objectFactorySupplier = new SingletonObjectFactorySupplier(
                objectFactoryServiceLoader);
            BackendSupplier backendSupplier = new BackendServiceLoader(classLoader, objectFactorySupplier);
            runnerSupplier = new SingletonRunnerSupplier(options, bus, backendSupplier, objectFactorySupplier);
        }
        return new CucumberExecutionContext(bus, exitStatus, runnerSupplier);
    }

    void startTestRun() {
        log.debug(() -> "Starting test run");
        // Problem: The JUnit Platform will always execute all engines that
        // participated in discovery. In combination with the JUnit Platform
        // Suite Engine this may result in CucumberEngine being executed
        // twice.
        //
        // One of these instances may not have discovered any tests and would
        // write empty reports. Therefor we do not invoke 'startTestRun' if
        // there are no tests to execute. Additionally, we defer creating
        // 'Plugins' until the last moment to avoid overwriting any output
        // files.
        //
        // Ideally 'Plugin' implementations would not start writing until they
        // received the `TestRunStarted` event but with the current setup this
        // is rather hard to change.
        //
        // Solution: Defer the instantiation of `Plugin` and everything else
        // until test execution starts.
        //
        // See: https://github.com/cucumber/cucumber-jvm/issues/2441
        context = createCucumberExecutionContext();
        context.startTestRun();
    }

    public void runBeforeAllHooks() {
        log.debug(() -> "Running before all hooks");
        context.runBeforeAllHooks();
    }

    public void beforeFeature(Feature feature) {
        context.beforeFeature(feature);
    }

    void runTestCase(Pickle pickle) {
        context.runTestCase((runner) -> {
            try (TestCaseResultObserver observer = observe(runner.getBus())) {
                log.debug(() -> "Executing test case " + pickle.getName());
                runner.runPickle(pickle);
                log.debug(() -> "Finished test case " + pickle.getName());
                observer.assertTestCasePassed();
            }
        });
    }

    public void runAfterAllHooks() {
        log.debug(() -> "Running after all hooks");
        context.runAfterAllHooks();
    }

    public void finishTestRun() {
        log.debug(() -> "Finishing test run");
        context.finishTestRun();
    }

}
