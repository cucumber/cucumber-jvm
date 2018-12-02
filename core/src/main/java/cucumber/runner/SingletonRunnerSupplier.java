package cucumber.runner;

import cucumber.runtime.BackendSupplier;
import io.cucumber.core.options.RunnerOptions;

/**
 * Returns a single unique runner.
 *
 * Not thread safe.
 */
public class SingletonRunnerSupplier implements RunnerSupplier {

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
