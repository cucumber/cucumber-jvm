package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventPublisher;

import java.util.Collection;

public interface EventBus extends EventPublisher {

    Long getTime();

    void send(Event event);

    void sendAll(Collection<Event> queue);

}
