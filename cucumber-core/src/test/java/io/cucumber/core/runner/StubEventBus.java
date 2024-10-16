package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.plugin.event.EventHandler;

import java.time.Instant;
import java.util.UUID;

public class StubEventBus implements EventBus {
    @Override
    public Instant getInstant() {
        return null;
    }

    @Override
    public UUID generateId() {
        return null;
    }

    @Override
    public <T> void send(T event) {

    }

    @Override
    public <T> void sendAll(Iterable<T> queue) {

    }

    @Override
    public <T> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {

    }

    @Override
    public <T> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {

    }
}
