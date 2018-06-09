package cucumber.runtime;

import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.TestCaseSyncEventBus;

import java.util.Collection;

public class RunnerSupplier implements Supplier<Runner> {

    private final Supplier<Collection<? extends Backend>> backendSupplier;
    private final RuntimeOptions runtimeOptions;
    private final Supplier<Glue> glueSupplier;
    private final EventBus eventBus;

    private final ThreadLocal<Runner> runner = new ThreadLocal<Runner>() {
        @Override
        protected Runner initialValue() {
            return createRunner();
        }
    };

    public RunnerSupplier(RuntimeOptions runtimeOptions, EventBus eventBus, Supplier<Collection<? extends Backend>> backendSupplier, Supplier<Glue> glueSupplier) {
        this.backendSupplier = backendSupplier;
        this.runtimeOptions = runtimeOptions;
        this.glueSupplier = glueSupplier;
        this.eventBus = eventBus;
    }

    @Override
    public Runner get() {
        return runner.get();
    }

    private Runner createRunner() {
        Collection<? extends Backend> backends = backendSupplier.get();
        if (backends.isEmpty()) {
            throw new CucumberException("No backends were found. Please make sure you have a backend module on your CLASSPATH.");
        }
        return new Runner(glueSupplier.get(), new TestCaseSyncEventBus(eventBus), backends, runtimeOptions);
    }

}
