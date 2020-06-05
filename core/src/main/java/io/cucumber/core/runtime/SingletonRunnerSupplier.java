package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runner.Options;
import io.cucumber.core.runner.Runner;

/**
 * Returns a single unique runner.
 * <p>
 * Not thread safe.
 */
public final class SingletonRunnerSupplier implements RunnerSupplier {

    private final BackendSupplier backendSupplier;
    private final Options runnerOptions;
    private final EventBus eventBus;
    private final ObjectFactorySupplier objectFactorySupplier;
    private final TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier;
    private Runner runner;

    public SingletonRunnerSupplier(
            Options runnerOptions,
            EventBus eventBus,
            BackendSupplier backendSupplier,
            ObjectFactorySupplier objectFactorySupplier, TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier
    ) {
        this.backendSupplier = backendSupplier;
        this.runnerOptions = runnerOptions;
        this.eventBus = eventBus;
        this.objectFactorySupplier = objectFactorySupplier;
        this.typeRegistryConfigurerSupplier = typeRegistryConfigurerSupplier;
    }

    @Override
    public Runner get() {
        if (runner == null) {
            runner = createRunner();
        }
        return runner;
    }

    private Runner createRunner() {
        return new Runner(
            eventBus,
            backendSupplier.get(),
            objectFactorySupplier.get(),
            typeRegistryConfigurerSupplier.get(),
            runnerOptions);
    }

}
