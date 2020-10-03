package io.cucumber.junit.platform.engine;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.CucumberExecutionContext;
import io.cucumber.core.runtime.ExitStatus;
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
import org.apiguardian.api.API;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.cucumber.core.runtime.SynchronizedEventBus.synchronize;

@API(status = API.Status.STABLE)
public final class CucumberEngineExecutionContext implements EngineExecutionContext {

    private static final Logger log = LoggerFactory.getLogger(CucumberEngineExecutionContext.class);
    private final CucumberEngineOptions options;
    private final CucumberExecutionContext context;
    private final EngineExecutionListener listener;

    CucumberEngineExecutionContext(ConfigurationParameters configuration, EngineExecutionListener listener) {
        this.listener = listener;

        Supplier<ClassLoader> classLoader = CucumberEngineExecutionContext.class::getClassLoader;
        log.debug(() -> "Parsing options");
        options = new CucumberEngineOptions(configuration);
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(options);
        EventBus bus = synchronize(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID));
        TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(
            classLoader, options);
        Plugins plugins = new Plugins(new PluginFactory(), options);
        ExitStatus exitStatus = new ExitStatus(options);
        plugins.addPlugin(exitStatus);

        RunnerSupplier runnerSupplier;
        if (options.isParallelExecutionEnabled()) {
            plugins.setSerialEventBusOnEventListenerPlugins(bus);
            ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(
                objectFactoryServiceLoader);
            BackendSupplier backendSupplier = new BackendServiceLoader(classLoader, objectFactorySupplier);
            runnerSupplier = new ThreadLocalRunnerSupplier(options, bus, backendSupplier, objectFactorySupplier,
                typeRegistryConfigurerSupplier);
        } else {
            plugins.setEventBusOnEventListenerPlugins(bus);
            ObjectFactorySupplier objectFactorySupplier = new SingletonObjectFactorySupplier(
                objectFactoryServiceLoader);
            BackendSupplier backendSupplier = new BackendServiceLoader(classLoader, objectFactorySupplier);
            runnerSupplier = new SingletonRunnerSupplier(options, bus, backendSupplier, objectFactorySupplier,
                typeRegistryConfigurerSupplier);
        }
        this.context = new CucumberExecutionContext(bus, exitStatus, runnerSupplier);
    }

    CucumberEngineOptions getOptions() {
        return options;
    }

    void startTestRun() {
        context.startTestRun();
    }

    public void beforeFeature(Feature feature) {
        context.beforeFeature(feature);
    }

    void runTestCase(Consumer<Runner> execution) {
        context.runTestCase(execution);
    }

    EngineExecutionListener getListener() {
        return listener;
    }

    public void finishTestRun() {
        context.finishTestRun();
    }

}
