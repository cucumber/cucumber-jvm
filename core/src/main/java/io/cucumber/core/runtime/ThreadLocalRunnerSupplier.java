package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.AbstractEventBus;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runner.Options;
import io.cucumber.core.runner.Runner;

import java.time.Instant;
import java.util.UUID;

/**
 * Creates a distinct runner for each calling thread. Each runner has its own
 * bus, backend- and glue-suppliers.
 * <p>
 * Each runners bus passes all events to the event bus of this supplier.
 */
public final class ThreadLocalRunnerSupplier implements RunnerSupplier {

    private final BackendSupplier backendSupplier;
    private final io.cucumber.core.runner.Options runnerOptions;
    private final SynchronizedEventBus sharedEventBus;
    private final ObjectFactorySupplier objectFactorySupplier;
    private final TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier;

    private final ThreadLocal<Runner> runners = ThreadLocal.withInitial(this::createRunner);

    public ThreadLocalRunnerSupplier(
            Options runnerOptions,
            EventBus sharedEventBus,
            BackendSupplier backendSupplier,
            ObjectFactorySupplier objectFactorySupplier,
            TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier
    ) {
        this.runnerOptions = runnerOptions;
        this.sharedEventBus = SynchronizedEventBus.synchronize(sharedEventBus);
        this.backendSupplier = backendSupplier;
        this.objectFactorySupplier = objectFactorySupplier;
        this.typeRegistryConfigurerSupplier = typeRegistryConfigurerSupplier;
    }

    @Override
    public Runner get() {
        return runners.get();
    }

    private Runner createRunner() {
        return new Runner(
            new LocalEventBus(sharedEventBus),
            backendSupplier.get(),
            objectFactorySupplier.get(),
            typeRegistryConfigurerSupplier.get(),
            runnerOptions);
    }

    private static final class LocalEventBus extends AbstractEventBus {

        private final SynchronizedEventBus parent;

        LocalEventBus(final SynchronizedEventBus parent) {
            this.parent = parent;
        }

        @Override
        public <T> void send(final T event) {
            super.send(event);
            parent.send(event);
        }

        @Override
        public Instant getInstant() {
            return parent.getInstant();
        }

        @Override
        public UUID generateId() {
            return parent.generateId();
        }

    }

}
