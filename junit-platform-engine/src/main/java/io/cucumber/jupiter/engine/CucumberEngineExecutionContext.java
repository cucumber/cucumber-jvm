package io.cucumber.jupiter.engine;

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
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;
import org.opentest4j.TestAbortedException;

import java.time.Clock;
import java.util.function.Supplier;

import static io.cucumber.plugin.event.Status.PASSED;

class CucumberEngineExecutionContext implements EngineExecutionContext {

    private static final Logger logger = LoggerFactory.getLogger(CucumberEngineExecutionContext.class);
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final EventBus bus;
    private final CucumberEngineOptions options;

    CucumberEngineExecutionContext(ConfigurationParameters configurationParameters) {

        Supplier<ClassLoader> classLoader = CucumberEngineExecutionContext.class::getClassLoader;
        logger.debug(() -> "Parsing options");
        this.options = new CucumberEngineOptions(configurationParameters);
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
        try (TestCaseResultObserver observer = observe(runner.getBus())) {
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

    private TestCaseResultObserver observe(EventPublisher bus) {
        return new TestCaseResultObserver(bus);
    }

    private Runner getRunner() {
        try {
            return runnerSupplier.get();
        } catch (Throwable e) {
            logger.error(e, () -> "Unable to start Cucumber");
            throw e;
        }
    }

    private class TestCaseResultObserver implements AutoCloseable {

        private final EventPublisher bus;
        private Result result;
        private final EventHandler<TestCaseFinished> testCaseFinished = new EventHandler<TestCaseFinished>() {
            @Override
            public void receive(TestCaseFinished event) {
                result = event.getResult();
            }
        };

        TestCaseResultObserver(EventPublisher bus) {
            this.bus = bus;
            bus.registerHandlerFor(TestCaseFinished.class, testCaseFinished);
        }

        @Override
        public void close() {
            bus.removeHandlerFor(TestCaseFinished.class, testCaseFinished);
        }

        void assertTestCasePassed() {
            if (result.getStatus().is(PASSED)) {
                return;
            }
            Throwable error = result.getError();
            if (result.getStatus().isOk(options.isStrict())) {
                // TODO: Fix strict mode
                // TODO: Include snippet here for undefined steps
                // TODO: Return and throw in caller. See JUNIT.
                // TODO: Distinguish between TestSkippedException and TestAbortedException
                if (error == null) {
                    throw new TestAbortedException();
                }
                throw new TestAbortedException(error.getMessage(), error);
            } else {
                ExceptionUtils.throwAsUncheckedException(error);
            }
        }
    }
}
