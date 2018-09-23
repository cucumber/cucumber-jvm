package io.cucumber.core.api.event;

import io.cucumber.core.api.plugin.Plugin;

/**
 * This is the interface you should implement if your plugin listens to cucumber execution events
 */
public interface EventListener extends Plugin {

    /**
     * Set the event publisher. The plugin can register event listeners with the publisher.
     *
     * @param publisher the event publisher
     */
    void setEventPublisher(EventPublisher publisher);

}
