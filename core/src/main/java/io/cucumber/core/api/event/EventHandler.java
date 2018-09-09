package io.cucumber.core.api.event;

public interface EventHandler<T extends Event> {

    void receive(T event);

}
