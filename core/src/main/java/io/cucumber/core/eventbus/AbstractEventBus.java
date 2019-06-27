package io.cucumber.core.eventbus;

import io.cucumber.core.event.Event;

public abstract class AbstractEventBus extends AbstractEventPublisher implements EventBus {

    @Override
    public void send(Event event) {
        super.send(event);
    }

    @Override
    public void sendAll(Iterable<Event> queue) {
        super.sendAll(queue);
    }
}
