package io.cucumber.plugin;

import io.cucumber.plugin.event.EventPublisher;
import org.apiguardian.api.API;

/**
 * Listens to pickle execution events. Can be used to implement reporters.
 * <p>
 * When cucumber executes test in parallel or in a framework that supports
 * parallel execution (e.g. JUnit or TestNG)
 * {@link io.cucumber.plugin.event.Event}s are stored and published in canonical
 * order after the test run has completed.
 *
 * @see io.cucumber.plugin.event.Event
 * @see ConcurrentEventListener
 */
@API(status = API.Status.STABLE)
public interface EventListener extends Plugin {

    /**
     * Set the event publisher. The plugin can register event listeners with the
     * publisher.
     *
     * @param publisher the event publisher
     */
    void setEventPublisher(EventPublisher publisher);

}
