package cucumber.api.event;

/**
 * This is the interface you should implement if you want your own custom
 * formatter.
 */
public interface EventListener {

    /**
     * Set the event publisher. The formatter can register event listeners with the publisher.
     *
     * @param publisher the event publisher
     */
    void setEventPublisher(EventPublisher publisher);

}
