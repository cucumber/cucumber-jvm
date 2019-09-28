package io.cucumber.plugin.event;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface EventHandler<T extends Event> {

    void receive(T event);

}
