package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.SnippetsSuggestedEvent;
import cucumber.api.event.TestCaseEvent;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.api.event.TestSourceRead;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class OrderedTestRunEventBus extends AbstractEventBus {

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
                List<Event> testCaseEvents = new ArrayList<Event>();
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
        Collections.sort(queue, new Comparator<List<Event>>() {

            private final List<?> weights = asList(TestRunStarted.class, TestSourceRead.class, SnippetsSuggestedEvent.class, TestCaseStarted.class, TestRunFinished.class);

            @Override
            public int compare(List<Event> o1, List<Event> o2) {
                Event event1 = o1.get(0);
                Event event2 = o2.get(0);
                int x = weights.indexOf(event1.getClass());
                int y = weights.indexOf(event2.getClass());

                if (x < y) return -1;
                else if (x > y) return 1;

                if (event1 instanceof TestCaseStarted && event2 instanceof TestCaseStarted) {
                    TestCaseStarted testCaseStarted1 = (TestCaseStarted) event1;
                    TestCaseStarted testCaseStarted2 = (TestCaseStarted) event2;
                    return uri(testCaseStarted1).compareTo(uri(testCaseStarted2));
                }

                return 0;
            }
        });

        ArrayList<Event> events = new ArrayList<Event>();
        for (List<Event> eventList : queue) {
            events.addAll(eventList);
        }

        return events;
    }

    public static String uri(TestCaseStarted testCaseStarted1) {
        return testCaseStarted1.testCase.getUri() + ":" + testCaseStarted1.testCase.getLine();
    }

}
