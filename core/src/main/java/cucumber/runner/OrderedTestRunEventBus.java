package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.TestCaseEvent;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

public class OrderedTestRunEventBus extends AbstractEventBus {

    private static final EventListComparator EVENT_LIST_COMPARATOR = new EventListComparator();

    private final EventBus parent;
    private List<List<Event>> queue = new ArrayList<List<Event>>();

    public OrderedTestRunEventBus(final EventBus parent) {
        this.parent = parent;
    }

    @Override
    public Long getTime() {
        return parent.getTime();
    }

    public void send(final Event event) {
        super.send(event);
        queue(event);

        if (event instanceof TestRunFinished) {
            parent.sendAll(flatten(queue));
            queue.clear();
        }
    }

    private void queue(final Event event) {
        if (event instanceof TestCaseEvent) {
            if (event instanceof TestCaseStarted) {
                final List<Event> testCaseEvents = new ArrayList<Event>();
                testCaseEvents.add(event);
                queue.add(testCaseEvents);
            } else {
                queue.get(queue.size() - 1).add(event);
            }
        } else {
            queue.add(singletonList(event));
        }
    }

    private static Iterable<Event> flatten(List<List<Event>> queue) {
        Collections.sort(queue, EVENT_LIST_COMPARATOR);

        final List<Event> events = new ArrayList<Event>();
        for (final List<Event> eventList : queue) {
            events.addAll(eventList);
        }

        return events;
    }

}
