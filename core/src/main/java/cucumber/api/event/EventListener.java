package cucumber.api.event;

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
