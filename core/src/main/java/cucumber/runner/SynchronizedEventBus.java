package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;

import java.util.Collections;

public class SynchronizedEventBus implements EventBus {

    private final EventBus delegate;

    public SynchronizedEventBus(final EventBus delegate) {
        this.delegate = delegate;
    }

    @Override
    public Long getTime() {
        return this.delegate.getTime();
    }

    @Override
    public synchronized void send(final Event event) {
        sendAll(Collections.singletonList(event));
    }

    protected synchronized void sendAll(final Iterable<Event> events) {
        for (final Event e : events) {
            this.delegate.send(e);
        }
    }

    @Override
    public <T extends Event> void registerHandlerFor(final Class<T> eventType, final EventHandler<T> handler) {
        this.delegate.registerHandlerFor(eventType, handler);
    }

    @Override
    public <T extends Event> void removeHandlerFor(final Class<T> eventType, final EventHandler<T> handler) {
        this.delegate.removeHandlerFor(eventType, handler);
    }
}
