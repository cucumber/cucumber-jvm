package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.TestRunFinished;

import java.util.ArrayList;
import java.util.List;

public class OrderedTestRunEventBus extends AbstractEventBus {

    private final EventBus parent;
    private List<Event> queue = new ArrayList<Event>();

    public OrderedTestRunEventBus(final EventBus parent) {
        this.parent = parent;
    }

    @Override
    public Long getTime() {
        return parent.getTime();
    }

    public void send(final Event event) {
        super.send(event);
        queue(event);
    }

    private void queue(final Event event) {
        queue.add(event);
        if (event instanceof TestRunFinished) {
            parent.sendAll(queue);
            queue.clear();
        }
    }

}
