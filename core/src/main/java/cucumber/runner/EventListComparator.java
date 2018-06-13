package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.SnippetsSuggestedEvent;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.api.event.TestSourceRead;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class EventListComparator implements Comparator<List<Event>> {

    private final List<Class<? extends Event>> weights = new ArrayList<Class<? extends Event>>();
    private final HashMap<Class<? extends Event>, Comparator<? extends Event>> typeComparators = new HashMap<Class<? extends Event>, Comparator<? extends Event>>();

    public EventListComparator() {
        weights.add(TestRunStarted.class);
        weights.add(TestSourceRead.class);
        weights.add(SnippetsSuggestedEvent.class);
        weights.add(TestCaseStarted.class);
        weights.add(TestRunFinished.class);

        typeComparators.put(TestCaseStarted.class, new TestCaseStartedComparator());
    }

    @Override
    public int compare(final List<Event> o1, final List<Event> o2) {
        final Event event1 = o1.get(0);
        final Event event2 = o2.get(0);
        int x = weights.indexOf(event1.getClass());
        int y = weights.indexOf(event2.getClass());

        if (x < y) {
            return -1;
        } else if (x > y) {
            return 1;
        } else if (event1.getClass().equals(event2.getClass()) && typeComparators.containsKey(event1.getClass())) {
            //noinspection unchecked: two classes are same time, and should inject comparator of correct type
            final Comparator<Event> comparator = (Comparator<Event>) typeComparators.get(event1.getClass());
            return comparator.compare(event1, event2);
        }
        return 0;
    }

}
