package io.cucumber.core.event;

import io.cucumber.core.api.event.Event;
import io.cucumber.core.api.event.EventPublisher;

public interface EventBus extends EventPublisher {

    Long getTime();
    
    Long getTimeStampMillis();

    void send(Event event);

    void sendAll(Iterable<Event> queue);

}
