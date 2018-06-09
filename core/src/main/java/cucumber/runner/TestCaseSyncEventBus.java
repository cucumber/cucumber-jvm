package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;
import cucumber.api.event.TestCaseEvent;
import cucumber.api.event.TestCaseFinished;

import java.util.ArrayList;
import java.util.List;

public class TestCaseSyncEventBus implements EventBus {

    // Static sync object so all instances can be locked together 
    private static final Object SYNC = new Object();
    private static final Class<? extends Event> FLUSH_EVENT = TestCaseFinished.class;

    private final List<Event> queue = new ArrayList<Event>();
    private final EventBus delegate;

    public TestCaseSyncEventBus(final EventBus delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(final Event event) {
        if (event instanceof TestCaseEvent) {
            queue(event);
        }
        else {
            delegate.send(event);
        }
    }

    void queue(final Event event) {
        queue.add(event);
        if (FLUSH_EVENT.isAssignableFrom(event.getClass())) {
            flushQueue();
        }
    }

    void flushQueue() {
        synchronized (SYNC) {
            for (Event event : queue) {
                delegate.send(event);
            }
            queue.clear();
        }
    }

    @Override
    public Long getTime() {
        return delegate.getTime();
    }

    @Override
    public <T extends Event> void registerHandlerFor(final Class<T> eventType, final EventHandler<T> handler) {
        delegate.registerHandlerFor(eventType, handler);
    }

    @Override
    public <T extends Event> void removeHandlerFor(final Class<T> eventType, final EventHandler<T> handler) {
        delegate.removeHandlerFor(eventType, handler);
    }
}
