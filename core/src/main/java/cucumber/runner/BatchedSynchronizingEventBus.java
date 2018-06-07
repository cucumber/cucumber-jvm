package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

abstract class BatchedSynchronizingEventBus implements EventBus {

    private final List<Event> queue = new ArrayList<Event>();
    private final EventBus delegate;
    private final Class<? extends Event> flushEvent;

    BatchedSynchronizingEventBus(final EventBus delegate, final Class<? extends Event> flushEvent) {
        this.delegate = delegate;
        this.flushEvent = flushEvent;
    }

    void queue(final Event event) {
        queue.add(event);
        if (event.getClass().isInstance(flushEvent)) {
            flushQueue();
        }
    }

    void flushQueue() {
        synchronized (getSyncObject()) {
            for (Event event : queue) {
                BatchedSynchronizingEventBus.this.send(event);
            }
            queue.clear();
        }
    }

    abstract Object getSyncObject();

    @Override
    public void send(final Event event) {
        delegate.send(event);
    }

    @Override
    public Long getTime() {
        return this.delegate.getTime();
    }

    @Override
    public <T extends Event> void registerHandlerFor(final Class<T> eventType, final EventHandler<T> handler) {
        delegate.registerHandlerFor(eventType, handler);
    }

    @Override
    public <T extends Event> void removeHandlerFor(final Class<T> eventType, final EventHandler<T> handler) {
        delegate.removeHandlerFor(eventType, handler);
    }
}

