package io.cucumber.core.event;

public interface EventHandler<T extends Event> {

    void receive(T event);

}
