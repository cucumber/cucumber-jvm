package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;

public final class SynchronizedEventBus implements EventBus {

    private final EventBus delegate;

    public static SynchronizedEventBus synchronize(EventBus eventBus) {
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
