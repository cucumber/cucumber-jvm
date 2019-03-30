package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventPublisher;

public interface EventBus extends EventPublisher {

    Long getTime();

    Long getTimeMillis();

    void send(Event event);

    void sendAll(Iterable<Event> queue);

}
