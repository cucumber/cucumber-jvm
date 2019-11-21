package io.cucumber.junit.platform.engine;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.runtime.TypeRegistryConfigurerSupplier;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

import java.time.Clock;
import java.util.function.Supplier;

class CucumberEngineExecutionContext implements EngineExecutionContext {

    private static final Logger logger = LoggerFactory.getLogger(CucumberEngineExecutionContext.class);
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final EventBus bus;

    CucumberEngineExecutionContext(ConfigurationParameters configurationParameters) {

        Supplier<ClassLoader> classLoader = CucumberEngineExecutionContext.class::getClassLoader;
        logger.debug(() -> "Parsing options");
        CucumberEngineOptions options = new CucumberEngineOptions(configurationParameters);
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(options);
        ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader);
        BackendSupplier backendSupplier = new BackendServiceLoader(classLoader, objectFactorySupplier);
        this.bus = new TimeServiceEventBus(Clock.systemUTC());
        new Plugins(new PluginFactory(), options);
        TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(classLoader, options);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(options, bus, backendSupplier, objectFactorySupplier, typeRegistryConfigurerSupplier);
    }

    void startTestRun() {
        logger.debug(() -> "Sending run test started event");
        bus.send(new TestRunStarted(bus.getInstant()));
    }

    void beforeFeature(CucumberFeature feature) {
        logger.debug(() -> "Sending test source read event for " + feature.getUri());
        bus.send(new TestSourceRead(bus.getInstant(), feature.getUri(), feature.getSource()));
    }

    void runTestCase(CucumberPickle pickle) {
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

