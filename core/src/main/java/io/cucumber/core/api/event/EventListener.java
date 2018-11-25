package io.cucumber.core.api.event;

import io.cucumber.core.api.plugin.Plugin;

/**
 * Listens to pickle execution events. Can be used to
 * implement reporters.
 * <p>
 * When cucumber executes test in parallel or in a framework
 * that supports parallel execution (e.g. JUnit or TestNG)
 * {@link cucumber.api.event.Event}s are stored and published
 * in @{@link Event#CANONICAL_ORDER} after the test run has
 * completed.
 *
 * @see Event
 * @see ConcurrentEventListener
 */
public interface EventListener extends Plugin {

    /**
     * Set the event publisher. The plugin can register event listeners with the publisher.
     *
     * @param publisher the event publisher
     */
    void setEventPublisher(EventPublisher publisher);

}
