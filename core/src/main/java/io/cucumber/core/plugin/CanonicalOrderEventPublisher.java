package io.cucumber.core.plugin;

import io.cucumber.core.event.Event;
import io.cucumber.core.event.TestRunFinished;
import io.cucumber.core.eventbus.AbstractEventPublisher;

import java.util.LinkedList;
import java.util.List;

final class CanonicalOrderEventPublisher extends AbstractEventPublisher {

    private final List<Event> queue = new LinkedList<>();

    public void handle(final Event event) {
        queue.add(event);
        if (event instanceof TestRunFinished) {
            queue.sort(new CanonicalEventOrder());
            sendAll(queue);
            queue.clear();
        }
    }

}
