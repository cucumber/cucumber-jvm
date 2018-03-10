
package io.cucucumber.jupiter.engine;

import cucumber.api.event.EventHandler;
import cucumber.api.event.TestCaseFinished;
import cucumber.runner.EventBus;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import gherkin.events.PickleEvent;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

import java.util.Collection;
import java.util.Collections;

import static io.cucucumber.jupiter.engine.Classloaders.getDefaultClassLoader;

public class CucumberEngineExecutionContext implements EngineExecutionContext {

    private static final Logger logger = LoggerFactory.getLogger(CucumberEngineExecutionContext.class);
    private final EngineExecutionListener executionListener;
    private final ConfigurationParameters configurationParameters;
    private final Runtime runtime;

    public CucumberEngineExecutionContext(EngineExecutionListener executionListener,
                                          ConfigurationParameters configurationParameters) {
        this.executionListener = executionListener;
        this.configurationParameters = configurationParameters;

        RuntimeOptions runtimeOptions = new RuntimeOptions("--plugin pretty");
        ClassLoader classLoader = getDefaultClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        this.runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
    }

    public EventBus getEventBus() {
        return runtime.getEventBus();
    }

    public ConfigurationParameters getConfigurationParameters() {
        return configurationParameters;
    }


    public EngineExecutionListener getExecutionListener() {
        return executionListener;
    }

    public void runPickle(PickleEvent pickleEvent) {
        final TestCaseFinished[] testCaseFinished = new TestCaseFinished[1];
        EventHandler<TestCaseFinished> testCaseFinishedEventHandler = event -> {
            testCaseFinished[0] = event;
        };
        runtime.getEventBus().registerHandlerFor(TestCaseFinished.class, testCaseFinishedEventHandler);
        runtime.getRunner().runPickle(pickleEvent);
        if (!testCaseFinished[0].result.isOk(true)) {
            //TODO:
        }
    }
}
