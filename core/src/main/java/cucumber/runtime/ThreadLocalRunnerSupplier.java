package cucumber.runtime;

import cucumber.runner.EventBus;
import cucumber.runner.Runner;

/**
 * Returns a distinct runner for each calling thread.
 */
public class ThreadLocalRunnerSupplier implements RunnerSupplier {

    private final BackendSupplier backendSupplier;
    private final RuntimeOptions runtimeOptions;
    private final GlueSupplier glueSupplier;
    private final EventBus eventBus;

    private final ThreadLocal<Runner> runners = new ThreadLocal<Runner>() {
        @Override
        protected Runner initialValue() {
            return createRunner();
        }
    };

    public ThreadLocalRunnerSupplier(
        RuntimeOptions runtimeOptions,
        EventBus eventBus,
        BackendSupplier backendSupplier,
        GlueSupplier glueSupplier
    ) {
        this.backendSupplier = backendSupplier;
        this.runtimeOptions = runtimeOptions;
        this.glueSupplier = glueSupplier;
        this.eventBus = eventBus;
    }

    @Override
    public Runner get() {
        return runners.get();
    }

    private Runner createRunner() {
        return new Runner(glueSupplier.get(), eventBus, backendSupplier.get(), runtimeOptions);
    }

}
