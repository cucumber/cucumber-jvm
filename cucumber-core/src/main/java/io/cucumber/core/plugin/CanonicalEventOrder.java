package io.cucumber.core.plugin;

import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.StepDefinedEvent;
import io.cucumber.plugin.event.TestCaseEvent;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceParsed;
import io.cucumber.plugin.event.TestSourceRead;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * When pickles are executed in parallel events can be produced with a partial
 * ordering.
 * <p>
 * The canonical order is the order in which these events would have been
 * generated had cucumber executed these pickles in a serial fashion.
 * <p>
 * In canonical order events are ordered by type and time stamp:
 * <ol>
 * <li>TestRunStarted
 * <li>TestSourceRead
 * <li>TestSourceParsed
 * <li>SnippetsSuggestedEvent
 * <li>StepDefinedEvent
 * <li>TestCaseEvent
 * <li>TestRunFinished
 * </ol>
 * <p>
 * As part of ordering events by type, TestCaseEvents are ordered by
 * <ol>
 * <li>uri
 * <li>line
 * <li>timestamp
 * </ol>
 */
final class CanonicalEventOrder implements Comparator<Event> {

    @Override
    public int compare(Event a, Event b) {
        return eventOrder.compare(a, b);
    }

    private static final Comparator<Event> eventOrder = Comparator
            .comparingInt(CanonicalEventOrder::eventOrder)
            .thenComparing(CanonicalEventOrder::testCaseEvents)
            .thenComparing(Event::getInstant);

    private static int testCaseEvents(Event a, Event b) {
        if (a instanceof TestCaseEvent && b instanceof TestCaseEvent) {
            return testCaseOrder.compare((TestCaseEvent) a, (TestCaseEvent) b);
        }
        return 0;
    }

    private static final Comparator<TestCaseEvent> testCaseOrder = Comparator
            .comparing(CanonicalEventOrder::testCaseUri)
            .thenComparingInt(CanonicalEventOrder::testCaseLine)
            .thenComparing(TestCaseEvent::getInstant);

    private static int testCaseLine(TestCaseEvent o) {
        return o.getTestCase().getLocation().getLine();
    }

    private static URI testCaseUri(TestCaseEvent o) {
        return o.getTestCase().getUri();
    }

    private static int eventOrder(Event o) {
        Class<? extends Event> eventClass = o.getClass();
        int index = findInFixedOrder(eventClass);
        if (index < 0) {
            throw new IllegalStateException(eventClass + " was not in " + fixedOrder);
        }
        return index;
    }

    private static final List<Class<? extends Event>> fixedOrder = asList(
        TestRunStarted.class,
        TestSourceRead.class,
        TestSourceParsed.class,
        SnippetsSuggestedEvent.class,
        StepDefinedEvent.class,
        TestCaseEvent.class,
        TestRunFinished.class);

    private static int findInFixedOrder(Class<? extends Event> o) {
        for (int i = 0; i < fixedOrder.size(); i++) {
            if (fixedOrder.get(i).isAssignableFrom(o)) {
                return i;
            }
        }
        return -1;
    }
}
