package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.AbstractEventPublisher;
import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.TestRunFinished;

import java.util.ArrayList;
import java.util.List;

final class CanonicalOrderEventPublisher extends AbstractEventPublisher {

    private final List<Event> queue = new ArrayList<>();

    public void handle(final Event event) {
        queue.add(event);
        if (event instanceof TestRunFinished) {
            queue.sort(new CanonicalEventOrder());
            sendAll(queue);
            queue.clear();
        }
    }

}
