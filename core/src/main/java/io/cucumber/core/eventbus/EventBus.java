package io.cucumber.core.eventbus;

import java.time.Instant;
import java.util.UUID;

import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.EventPublisher;

public interface EventBus extends EventPublisher {

    Instant getInstant();

    UUID generateId();

    void send(Event event);

    void sendAll(Iterable<Event> queue);

}
