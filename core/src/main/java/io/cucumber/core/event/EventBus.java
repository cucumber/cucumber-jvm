package io.cucumber.core.event;

import cucumber.api.event.Event;
import cucumber.api.event.EventPublisher;

public interface EventBus extends EventPublisher {

    Long getTime();

    void send(Event event);

    void sendAll(Iterable<Event> queue);

}
