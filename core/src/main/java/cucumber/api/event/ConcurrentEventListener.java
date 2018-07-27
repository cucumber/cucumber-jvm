package cucumber.api.event;

import cucumber.api.Plugin;

/**
 * When cucumber executes test in parallel or in a framework
 * that supports parallel execution (e.g. JUnit or TestNG)
 * {@link cucumber.api.TestCase} events from different
 * pickles may interleave.
 * <p>
 * This interface marks an {@link EventListener} as capable of
 * understanding interleaved pickle events.
 * <p>
 * While running tests in parallel cucumber makes the
 * following guarantees.
 * <p>
 * 1. The event publisher is synchronized. Events are not
 * handled concurrently.
 * <p>
 * 2. For test cases executed on different threads the callbacks
 * registered on the event publisher will be called by
 * different threads. I.e.  Thread.currentThread()
 * will return different a different thread for two test cases
 * executed on a different thread (but not necessarily the
 * executing thread).
 * <p>
 *
 * @see Event
 */
public interface ConcurrentEventListener extends Plugin {

    /**
     * Set the event publisher. The formatter can register event listeners with the publisher.
     *
     * @param publisher the event publisher
     */
    void setEventPublisher(EventPublisher publisher);

}
