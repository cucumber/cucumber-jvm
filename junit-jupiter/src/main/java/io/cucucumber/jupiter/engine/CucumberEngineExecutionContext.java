
package io.cucucumber.jupiter.engine;

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
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

class CucumberEngineExecutionContext implements EngineExecutionContext {

    private static final Logger logger = LoggerFactory.getLogger(CucumberEngineExecutionContext.class);
    private final EngineExecutionListener executionListener;
    private final ConfigurationParameters configurationParameters;
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final EventBus bus;
    private final Plugins plugins;

    CucumberEngineExecutionContext(EngineExecutionListener executionListener,
                                   ConfigurationParameters configurationParameters) {
        this.executionListener = executionListener;
        this.configurationParameters = configurationParameters;

        ClassLoader classLoader = Classloaders.getDefaultClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);

        logger.debug(() -> "Parsing options");
        RuntimeOptions runtimeOptions = new RuntimeOptions("--plugin pretty");
        BackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        this.bus = new TimeServiceEventBus(TimeService.SYSTEM);
        this.plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier);
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
        runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);

    }

    void beforeFeature(CucumberFeature feature) {
        logger.debug(() -> "Sending test source read event for " + feature.getUri());
        feature.sendTestSourceRead(bus);
    }

    void runPickle(PickleEvent pickleEvent) {
        logger.debug(() -> "Executing pickle " + pickleEvent.pickle.getName());
        runnerSupplier.get().runPickle(pickleEvent);
    }

    void finishTestRun() {
        logger.debug(() -> "Sending test run finished event");
        bus.send(new TestRunFinished(bus.getTime()));
    }
}
