package io.cucumber.core.runtime;

import io.cucumber.core.backend.BackendSupplier;
import io.cucumber.core.backend.ObjectFactorySupplier;
import io.cucumber.core.event.EventBus;
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
    private final ObjectFactorySupplier objectFactory;
    private Runner runner;


    public SingletonRunnerSupplier(
        Options runnerOptions,
        EventBus eventBus,
        BackendSupplier backendSupplier,
        ObjectFactorySupplier objectFactory) {
        this.backendSupplier = backendSupplier;
        this.runnerOptions = runnerOptions;
        this.eventBus = eventBus;
        this.objectFactory = objectFactory;
    }

    @Override
    public Runner get() {
        if (runner == null) {
            runner = createRunner();
        }
        return runner;
    }

    private Runner createRunner() {
        return new Runner(eventBus, backendSupplier.get(), objectFactory.get(), runnerOptions);
    }

}
