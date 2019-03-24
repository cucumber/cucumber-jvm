package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventPublisher;

public interface EventBus extends EventPublisher {

    Long getTime();
    
    Long getTimeStampMillis();

    void send(Event event);

    void sendAll(Iterable<Event> queue);

}
