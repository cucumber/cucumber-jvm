package io.cucumber.core.plugin;

import io.cucumber.core.event.Event;
import io.cucumber.core.event.EventPublisher;
import org.apiguardian.api.API;

/**
 * Listens to pickle execution events. Can be used to
 * implement reporters.
 * <p>
 * When cucumber executes test in parallel or in a framework
 * that supports parallel execution (e.g. JUnit or TestNG)
 * {@link Event}s are stored and published
 * in @{@link io.cucumber.core.plugin.CanonicalEventOrder} after the test run has
 * completed.
 *
 * @see Event
 * @see ConcurrentEventListener
 */
@API(status = API.Status.STABLE)
public interface EventListener extends Plugin {

    /**
     * Set the event publisher. The plugin can register event listeners with the publisher.
     *
     * @param publisher the event publisher
     */
    void setEventPublisher(EventPublisher publisher);

}
