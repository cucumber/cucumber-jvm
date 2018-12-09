package io.cucucumber.jupiter.engine;

import cucumber.api.Result;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.BackendModuleBackendSupplier;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;
import org.opentest4j.TestAbortedException;

import static cucumber.api.Result.Type.PASSED;

class CucumberEngineExecutionContext implements EngineExecutionContext {

    private static final Logger logger = LoggerFactory.getLogger(CucumberEngineExecutionContext.class);
    private final EngineExecutionListener executionListener;
    private final ConfigurationParameters configurationParameters;
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final EventBus bus;
    private final Plugins plugins;
    private final CucumberEngineOptions options;

    CucumberEngineExecutionContext(EngineExecutionListener executionListener,
                                   ConfigurationParameters configurationParameters) {
        this.executionListener = executionListener;
        this.configurationParameters = configurationParameters;

        ClassLoader classLoader = ClassLoaders.getDefaultClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);

        logger.debug(() -> "Parsing options");
        this.options = new CucumberEngineOptions(configurationParameters);
        BackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, options);
        this.bus = new TimeServiceEventBus(TimeService.SYSTEM);
        this.plugins = new Plugins(classLoader, new PluginFactory(), bus, options);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(options, bus, backendSupplier);
    }

    public ConfigurationParameters getConfigurationParameters() {
        return configurationParameters;
    }

    public EngineExecutionListener getExecutionListener() {
        return executionListener;
    }

    void startTestRun() {
        logger.debug(() -> "Sending run test started event");
        bus.send(new TestRunStarted(bus.getTime()));
        logger.debug(() -> "Reporting step definitions");
        final StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
        getRunner().reportStepDefinitions(stepDefinitionReporter);
    }

    void beforeFeature(CucumberFeature feature) {
        logger.debug(() -> "Sending test source read event for " + feature.getUri());
        feature.sendTestSourceRead(bus);
    }

    void runTestCase(PickleEvent pickleEvent) {
        Runner runner = getRunner();
        try (TestCaseResultObserver observer = observe(runner.getBus())) {
            logger.debug(() -> "Executing test case " + pickleEvent.pickle.getName());
            runner.runPickle(pickleEvent);
            logger.debug(() -> "Finished test case " + pickleEvent.pickle.getName());
            observer.assertTestCasePassed();
        }
    }

    void finishTestRun() {
        logger.debug(() -> "Sending test run finished event");
        bus.send(new TestRunFinished(bus.getTime()));
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
                result = event.result;
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
            if (result.is(PASSED)) {
                return;
            }
            Throwable error = result.getError();
            if (result.isOk(options.isStrict())) {
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
