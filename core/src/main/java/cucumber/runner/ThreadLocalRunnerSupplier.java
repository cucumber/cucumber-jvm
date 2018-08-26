package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;
import cucumber.runner.AbstractEventBus;
import cucumber.runner.EventBus;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.RuntimeOptions;

/**
 * Creates a distinct runner for each calling thread. Each runner has its own bus, backend- and glue-suppliers.
 * <p>
 * Each runners bus passes all events to the event bus of this supplier.
 */
public class ThreadLocalRunnerSupplier implements RunnerSupplier {

    private final BackendSupplier backendSupplier;
    private final RuntimeOptions runtimeOptions;
    private final SynchronizedEventBus sharedEventBus;

    private final ThreadLocal<Runner> runners = new ThreadLocal<Runner>() {
        @Override
        protected Runner initialValue() {
            return createRunner();
        }
    };

    public ThreadLocalRunnerSupplier(
        RuntimeOptions runtimeOptions,
        EventBus sharedEventBus,
        BackendSupplier backendSupplier
    ) {
        this.runtimeOptions = runtimeOptions;
        this.sharedEventBus = SynchronizedEventBus.synchronize(sharedEventBus);
        this.backendSupplier = backendSupplier;
    }

    @Override
    public Runner get() {
        return runners.get();
    }

    private Runner createRunner() {
        return new Runner(new LocalEventBus(sharedEventBus), backendSupplier.get(), runtimeOptions);
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
