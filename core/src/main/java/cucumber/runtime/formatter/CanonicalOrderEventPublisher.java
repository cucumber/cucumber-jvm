package cucumber.runtime.formatter;

import cucumber.api.event.Event;
import cucumber.api.event.TestRunFinished;
import cucumber.runner.AbstractEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CanonicalOrderEventPublisher extends AbstractEventPublisher {

    private final List<Event> queue = new ArrayList<Event>();

    public void handle(final Event event) {
        queue.add(event);
        if (event instanceof TestRunFinished) {
            Collections.sort(queue, Event.CANONICAL_ORDER);
            sendAll(queue);
            queue.clear();
        }
    }

}
