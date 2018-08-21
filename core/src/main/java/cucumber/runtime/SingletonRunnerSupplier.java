package cucumber.runtime;

import cucumber.runner.EventBus;
import cucumber.runner.Runner;

/**
 * Returns a distinct runner.
 */
public class SingletonRunnerSupplier implements RunnerSupplier {

    private final BackendSupplier backendSupplier;
    private final RuntimeOptions runtimeOptions;
    private final EventBus eventBus;
    private Runner runner;


    SingletonRunnerSupplier(
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
