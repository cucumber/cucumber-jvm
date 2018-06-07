package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.TestCaseFinished;

public class DoubleEventBus extends BatchedSynchronizingEventBus {

    // Static sync object so all instances can be locked together
    private static final Object SYNC = new Object();

    public DoubleEventBus(final EventBus delegate) {
        super(delegate, TestCaseFinished.class);
    }

    @Override
    Object getSyncObject() {
        return SYNC;
    }

    @Override
    public void send(final Event event) {
        super.send(event);
        queue(event);
    }

}
