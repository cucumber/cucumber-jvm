package io.cucumber.core.plugin;

import io.cucumber.core.api.event.Event;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.event.AbstractEventPublisher;

import java.util.Collections;
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
