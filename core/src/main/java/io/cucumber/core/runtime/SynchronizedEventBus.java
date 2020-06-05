package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.plugin.event.EventHandler;

import java.time.Instant;
import java.util.UUID;

public final class SynchronizedEventBus implements EventBus {

    private final EventBus delegate;

    private SynchronizedEventBus(final EventBus delegate) {
        this.delegate = delegate;
    }

    public static SynchronizedEventBus synchronize(EventBus eventBus) {
        if (eventBus instanceof SynchronizedEventBus) {
            return (SynchronizedEventBus) eventBus;
        }

        return new SynchronizedEventBus(eventBus);
    }

    @Override
    public synchronized <T> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        delegate.registerHandlerFor(eventType, handler);
    }

    @Override
    public synchronized <T> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        delegate.removeHandlerFor(eventType, handler);
    }

    @Override
    public Instant getInstant() {
        return delegate.getInstant();
    }

    @Override
    public UUID generateId() {
        return delegate.generateId();
    }

    @Override
    public synchronized <T> void send(final T event) {
        delegate.send(event);
    }

    @Override
    public synchronized <T> void sendAll(final Iterable<T> events) {
        delegate.sendAll(events);
    }

}
