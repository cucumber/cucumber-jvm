package cucumber.api.event;

/**
 * This is the interface you should implement if your plugin listens to cucumber execution events
 *
 * TODO: Doc the concurrent part
 *
 */
public interface ConcurrentEventListener {

    /**
     * Set the event publisher. The formatter can register event listeners with the publisher.
     *
     * @param publisher the event publisher
     */
    void setEventPublisher(EventPublisher publisher);

}
