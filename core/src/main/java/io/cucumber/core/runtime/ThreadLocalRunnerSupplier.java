package io.cucumber.core.runtime;

import io.cucumber.core.api.event.Event;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.backend.BackendSupplier;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.ObjectFactorySupplier;
import io.cucumber.core.event.AbstractEventBus;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.options.RunnerOptions;
import io.cucumber.core.runner.Runner;

/**
 * Creates a distinct runner for each calling thread. Each runner has its own bus, backend- and glue-suppliers.
 * <p>
 * Each runners bus passes all events to the event bus of this supplier.
 */
public final class ThreadLocalRunnerSupplier implements RunnerSupplier {

    private final BackendSupplier backendSupplier;
    private final RunnerOptions runnerOptions;
    private final SynchronizedEventBus sharedEventBus;
    private final ObjectFactorySupplier objectFactory;

    private final ThreadLocal<Runner> runners = ThreadLocal.withInitial(this::createRunner);

    public ThreadLocalRunnerSupplier(
        RunnerOptions runnerOptions,
        EventBus sharedEventBus,
        BackendSupplier backendSupplier,
        ObjectFactorySupplier objectFactory) {
        this.runnerOptions = runnerOptions;
        this.sharedEventBus = SynchronizedEventBus.synchronize(sharedEventBus);
        this.backendSupplier = backendSupplier;
        this.objectFactory = objectFactory;
    }

    @Override
    public Runner get() {
        return runners.get();
    }

    private Runner createRunner() {
        return new Runner(new LocalEventBus(sharedEventBus), backendSupplier.get(), objectFactory.get(), runnerOptions);
    }

    private static final class LocalEventBus extends AbstractEventBus {

        private final SynchronizedEventBus parent;

        LocalEventBus(final SynchronizedEventBus parent) {
            this.parent = parent;
        }

        @Override
        public Long getTime() {
            return parent.getTime();
        }

        @Override
        public void send(final Event event) {
            super.send(event);
            parent.send(event);
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
        public synchronized Long getTime() {
            return delegate.getTime();
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
    }
}
