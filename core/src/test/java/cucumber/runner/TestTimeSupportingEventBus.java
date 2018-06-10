package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;
import cucumber.api.event.TestStepStarted;

import java.util.ArrayList;
import java.util.List;

public class TestTimeSupportingEventBus extends AbstractEventBus {

    private final List<EventHandler> handlers = new ArrayList<EventHandler>();
    private final EventBus delegate;

    public TestTimeSupportingEventBus(final EventBus delegate) {
        this.delegate = delegate;
    }

    @Override
    public Long getTime() {
        return delegate.getTime();
    }

    @Override
    public void send(final Event event) {
        if (event instanceof TestStepStarted) {
            for (final EventHandler handler : handlers) {
                //noinspection unchecked: protected by registerHandlerFor
                handler.receive(event);
            }
        }
        delegate.send(event);
    }

    @Override
    public <T extends Event> void registerHandlerFor(final Class<T> eventType, final EventHandler<T> handler) {
        super.registerHandlerFor(eventType, handler);
        delegate.registerHandlerFor(eventType, handler);
    }

    @Override
    public <T extends Event> void removeHandlerFor(final Class<T> eventType, final EventHandler<T> handler) {
        super.removeHandlerFor(eventType, handler);
        delegate.removeHandlerFor(eventType, handler);
    }
}
