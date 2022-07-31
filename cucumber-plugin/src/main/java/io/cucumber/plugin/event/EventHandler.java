package io.cucumber.plugin.event;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface EventHandler<T> {

    void receive(T event);

}
