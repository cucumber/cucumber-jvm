package io.cucumber.core.runtime;

import io.cucumber.core.backend.BackendSupplier;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runtime.RunnerSupplier;

/**
 * Returns a single unique runner.
 * <p>
 * Not thread safe.
 */
public final class SingletonRunnerSupplier implements RunnerSupplier {

    private final BackendSupplier backendSupplier;
    private final RunnerOptions runnerOptions;
    private final EventBus eventBus;
    private Runner runner;


    public SingletonRunnerSupplier(
        RunnerOptions runnerOptions,
        EventBus eventBus,
        BackendSupplier backendSupplier
    ) {
        this.backendSupplier = backendSupplier;
        this.runnerOptions = runnerOptions;
        this.eventBus = eventBus;
    }

    @Override
    public Runner get() {
        if (runner == null) {
            runner = createRunner();
        }
        return runner;
    }

    private Runner createRunner() {
        return new Runner(eventBus, backendSupplier.get(), runnerOptions);
    }

}
