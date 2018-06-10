package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.TestCaseEvent;
import cucumber.api.event.TestCaseFinished;

import java.util.ArrayList;
import java.util.List;

public class TestCaseEventBus extends SynchronizedEventBus {

    private final ThreadLocal<List<Event>> queue = new ThreadLocal<List<Event>>() {
        @Override
        protected List<Event> initialValue() {
            return new ArrayList<Event>();
        }
    };

    public TestCaseEventBus(final EventBus delegate) {
        super(delegate);
    }

    @Override
    public void send(final Event event) {
        if (event instanceof TestCaseEvent) {
            queue(event);
        }
        else {
            super.send(event);
        }
    }

    void queue(final Event event) {
        queue.get().add(event);
        if (TestCaseFinished.class.isAssignableFrom(event.getClass())) {
            super.sendAll(queue.get());
            queue.get().clear();
        }
    }

}
