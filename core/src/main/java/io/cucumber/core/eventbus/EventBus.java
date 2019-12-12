package io.cucumber.core.eventbus;

import java.time.Instant;
import java.util.UUID;

import io.cucumber.plugin.event.EventPublisher;

public interface EventBus extends EventPublisher {

    Instant getInstant();

    UUID generateId();

    <T> void send(T event);

    <T> void sendAll(Iterable<T> queue);

}
