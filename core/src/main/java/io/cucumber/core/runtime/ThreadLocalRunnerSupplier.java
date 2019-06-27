package io.cucumber.core.runtime;

import java.time.Instant;

import io.cucumber.core.event.Event;
import io.cucumber.core.event.EventHandler;
import io.cucumber.core.eventbus.AbstractEventBus;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runner.Options;
import io.cucumber.core.runner.Runner;

/**
 * Creates a distinct runner for each calling thread. Each runner has its own bus, backend- and glue-suppliers.
 * <p>
 * Each runners bus passes all events to the event bus of this supplier.
 */
public final class ThreadLocalRunnerSupplier implements RunnerSupplier {

    private final BackendSupplier backendSupplier;
    private final io.cucumber.core.runner.Options runnerOptions;
    private final SynchronizedEventBus sharedEventBus;
    private final ObjectFactorySupplier objectFactorySupplier;
    private final TypeRegistrySupplier typeRegistrySupplier;

    private final ThreadLocal<Runner> runners = ThreadLocal.withInitial(this::createRunner);

    public ThreadLocalRunnerSupplier(
        Options runnerOptions,
        EventBus sharedEventBus,
        BackendSupplier backendSupplier,
        ObjectFactorySupplier objectFactorySupplier, TypeRegistrySupplier typeRegistrySupplier) {
        this.runnerOptions = runnerOptions;
        this.sharedEventBus = SynchronizedEventBus.synchronize(sharedEventBus);
        this.backendSupplier = backendSupplier;
        this.objectFactorySupplier = objectFactorySupplier;
        this.typeRegistrySupplier = typeRegistrySupplier;
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
            typeRegistrySupplier.get(),
            runnerOptions
        );
    }

    private static final class LocalEventBus extends AbstractEventBus {

        private final SynchronizedEventBus parent;

        LocalEventBus(final SynchronizedEventBus parent) {
            this.parent = parent;
        }

        @Override
        public void send(final Event event) {
            super.send(event);
            parent.send(event);
        }

        @Override
        public Instant getInstant() {
            return parent.getInstant();
        }
    }

    private static final class SynchronizedEventBus implements EventBus {

        private final EventBus delegate;

        static SynchronizedEventBus synchronize(EventBus eventBus) {
            if (eventBus instanceof SynchronizedEventBus) {
                return (SynchronizedEventBus) eventBus;
            }

            return new SynchronizedEventBus(eventBus);
        }

        private SynchronizedEventBus(final EventBus delegate) {
            this.delegate = delegate;
        }

        @Override
        public synchronized void send(final Event event) {
            delegate.send(event);
        }

        @Override
        public synchronized void sendAll(final Iterable<Event> events) {
            delegate.sendAll(events);
        }

        @Override
        public synchronized <T extends Event> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {
            delegate.registerHandlerFor(eventType, handler);
        }

        @Override
        public synchronized <T extends Event> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {
            delegate.removeHandlerFor(eventType, handler);
        }

        @Override
        public Instant getInstant() {
            return delegate.getInstant();
        }
    }
}
