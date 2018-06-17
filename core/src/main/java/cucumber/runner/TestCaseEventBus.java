package cucumber.runner;

import cucumber.api.event.Event;

public class TestCaseEventBus extends AbstractEventBus {

    private final SynchronizedEventBus parent;

    public TestCaseEventBus(final SynchronizedEventBus parent) {
        this.parent = parent;
    }

    @Override
    public Long getTime() {
        return parent.getTime();
    }

    @Override
    public void send(final Event event) {
        super.send(event);
        parent.send(event);
    }
}
