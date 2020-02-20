package io.cucumber.junit.platform.engine;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier;
import io.cucumber.core.runtime.SingletonObjectFactorySupplier;
import io.cucumber.core.runtime.SingletonRunnerSupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.runtime.TypeRegistryConfigurerSupplier;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceParsed;
import io.cucumber.plugin.event.TestSourceRead;
import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

@API(status = API.Status.STABLE)
public final class CucumberEngineExecutionContext implements EngineExecutionContext {

    private static final Logger logger = LoggerFactory.getLogger(CucumberEngineExecutionContext.class);
    private final RunnerSupplier runnerSupplier;
    private final EventBus bus;
    private final CucumberEngineOptions options;

    CucumberEngineExecutionContext(ConfigurationParameters configurationParameters) {

        Supplier<ClassLoader> classLoader = CucumberEngineExecutionContext.class::getClassLoader;
        logger.debug(() -> "Parsing options");
        options = new CucumberEngineOptions(configurationParameters);
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(options);
        this.bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(classLoader, options);
        Plugins plugins = new Plugins(new PluginFactory(), options);

        if (options.isParallelExecutionEnabled()) {
            plugins.setSerialEventBusOnEventListenerPlugins(bus);
            ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader);
            BackendSupplier backendSupplier = new BackendServiceLoader(classLoader, objectFactorySupplier);
            this.runnerSupplier = new ThreadLocalRunnerSupplier(options, bus, backendSupplier, objectFactorySupplier, typeRegistryConfigurerSupplier);
        } else {
            plugins.setEventBusOnEventListenerPlugins(bus);
            ObjectFactorySupplier objectFactorySupplier = new SingletonObjectFactorySupplier(objectFactoryServiceLoader);
            BackendSupplier backendSupplier = new BackendServiceLoader(classLoader, objectFactorySupplier);
            this.runnerSupplier = new SingletonRunnerSupplier(options, bus, backendSupplier, objectFactorySupplier, typeRegistryConfigurerSupplier);
        }
    }

    CucumberEngineOptions getOptions() {
        return options;
    }

    void startTestRun() {
        logger.debug(() -> "Sending run test started event");
        bus.send(new TestRunStarted(bus.getInstant()));
    }

    void beforeFeature(Feature feature) {
        logger.debug(() -> "Sending test source read event for " + feature.getUri());
        // Invoked concurrently.
        EventBus bus = getRunner().getBus();
        bus.send(new TestSourceRead(this.bus.getInstant(), feature.getUri(), feature.getSource()));
        bus.send(new TestSourceParsed(this.bus.getInstant(), feature.getUri(), feature));
    }

    void runTestCase(Pickle pickle) {
        Runner runner = getRunner();
        try (TestCaseResultObserver observer = TestCaseResultObserver.observe(runner.getBus())) {
            logger.debug(() -> "Executing test case " + pickle.getName());
            runner.runPickle(pickle);
            logger.debug(() -> "Finished test case " + pickle.getName());
            observer.assertTestCasePassed();
        }
    }

    void finishTestRun() {
        logger.debug(() -> "Sending test run finished event");
        bus.send(new TestRunFinished(bus.getInstant()));
    }

    private Runner getRunner() {
        try {
            return runnerSupplier.get();
        } catch (Throwable e) {
            logger.error(e, () -> "Unable to start Cucumber");
            throw e;
        }
    }

}

