package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.TestCaseEvent;
import cucumber.api.event.TestCaseFinished;

public class TestCaseSyncEventBus extends BatchedSynchronizingEventBus {
    
    // Static sync object so all instances can be locked together 
    private static final Object SYNC = new Object();

    public TestCaseSyncEventBus(final EventBus delegate) {
        super(delegate, TestCaseFinished.class);
    }

    @Override
    Object getSyncObject() {
        return SYNC;
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
}
