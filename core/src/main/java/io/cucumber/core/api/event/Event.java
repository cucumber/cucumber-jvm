package io.cucumber.core.api.event;

import java.util.Comparator;

public interface Event {

    /**
     * When pickles are executed in parallel events can be
     * produced with a partial ordering.
     * <p>
     * The canonical order is the order in which these events
     * would have been generated had cucumber executed these
     * pickles in a serial fashion.
     * <p>
     * In canonical order events are first ordered by type:
     * <ol>
     * <li>TestRunStarted
     * <li>TestSourceRead
     * <li>SnippetsSuggestedEvent
     * <li>TestCaseEvent
     * <li>TestRunFinished
     * </ol>
     * <p>
     * Then TestCaseEvents are ordered by
     * <ol>
     * <li>uri
     * <li>line
     * <li>timestamp
     * </ol>
     */
    Comparator<Event> CANONICAL_ORDER = new CanonicalEventOrder();

    /**
     * Returns timestamp in nano seconds since an arbitrary start time.
     *
     * @return timestamp in nano seconds
     * @see System#nanoTime()
     * @deprecated prefer {@link TimeStampedEvent#getTimeStampMillis()}
     */
    @Deprecated
    Long getTimeStamp();

}
