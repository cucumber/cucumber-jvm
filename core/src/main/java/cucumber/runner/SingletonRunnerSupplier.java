package cucumber.runner;

import cucumber.runtime.BackendSupplier;
import cucumber.runtime.RuntimeOptions;

/**
 * Returns a single unique runner.
 *
 * Not thread safe.
 */
public class SingletonRunnerSupplier implements RunnerSupplier {

    private final BackendSupplier backendSupplier;
    private final RuntimeOptions runtimeOptions;
    private final EventBus eventBus;
    private Runner runner;


    public SingletonRunnerSupplier(
        RuntimeOptions runtimeOptions,
        EventBus eventBus,
        BackendSupplier backendSupplier
    ) {
        this.backendSupplier = backendSupplier;
        this.runtimeOptions = runtimeOptions;
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
        return new Runner(eventBus, backendSupplier.get(), runtimeOptions);
    }

}
