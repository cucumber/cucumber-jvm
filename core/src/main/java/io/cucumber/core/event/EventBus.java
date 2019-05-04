package io.cucumber.core.event;

import java.time.Instant;

import io.cucumber.core.api.event.Event;
import io.cucumber.core.api.event.EventPublisher;

public interface EventBus extends EventPublisher {

    //gazler
//    Long getTime();
//
//    Long getTimeMillis();
    
    Instant getTimeInstant();

    void send(Event event);

    void sendAll(Iterable<Event> queue);

}
