package cucumber.api.event;

import cucumber.api.event.EventPublisher;

/**
 * This is the interface you should implement if you want your own custom
 * formatter.
 */
public interface EventListener {

    /**
     * Set the event bus that the formatter can register event listeners in.
     */
    void setEventPublisher(EventPublisher publisher);

}
