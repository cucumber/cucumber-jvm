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
        //TODO: need to double check this with mpkorstanje
        //TODO: as before it was new BatchedEventBus(this)
        //TODO: which would create a new EventBus with an empty hashset and thus the send would do nothing, from my thinking
        //TODO: as cucumber.api.junit.Cucumber.Cucumber used EventBus (now DefaultEventBus)
        super.send(event);
        queue(event);
    }

}
