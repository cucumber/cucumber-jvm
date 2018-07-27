package cucumber.runtime;

import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.SynchronizedEventBus;
import cucumber.runner.TestCaseEventBus;

import static cucumber.runner.SynchronizedEventBus.synchronize;

/**
 * Creates a distinct runner for each calling thread. Each runner has its own bus, backend- and glue-suppliers.
 * <p>
 * Each runners bus passes all events to the event bus of this supplier.
 */
public class ThreadLocalRunnerSupplier implements RunnerSupplier {

    private final BackendSupplier backendSupplier;
    private final RuntimeOptions runtimeOptions;
    private final GlueSupplier glueSupplier;
    private final SynchronizedEventBus eventBus;

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
        this.eventBus = synchronize(eventBus);
    }

    @Override
    public Runner get() {
        return runners.get();
    }

    private Runner createRunner() {
        return new Runner(glueSupplier.get(), new TestCaseEventBus(eventBus), backendSupplier.get(), runtimeOptions);
    }

}
