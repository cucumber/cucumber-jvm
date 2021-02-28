package io.cucumber.core.eventbus;

public abstract class AbstractEventBus extends AbstractEventPublisher implements EventBus {

    @Override
    public <T> void sendAll(Iterable<T> queue) {
        super.sendAll(queue);
    }

    @Override
    public <T> void send(T event) {
        super.send(event);
    }

}
