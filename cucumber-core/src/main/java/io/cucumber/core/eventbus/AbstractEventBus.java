package io.cucumber.core.eventbus;

public abstract class AbstractEventBus extends AbstractEventPublisher implements EventBus {

    /**
     * Send all events.
     * <p>
     * May be overridden, but must be called.
     */
    @Override
    public <T> void sendAll(Iterable<T> queue) {
        super.sendAll(queue);
    }

    /**
     * Send a single event.
     * <p>
     * May be overridden, but must be called.
     */
    @Override
    public <T> void send(T event) {
        super.send(event);
    }

}
