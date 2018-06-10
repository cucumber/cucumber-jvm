package cucumber.runner;

import cucumber.api.event.Event;

import java.util.Collection;

public final class SynchronizedEventBus extends AbstractEventBus {

    private final EventBus delegate;

    public static SynchronizedEventBus synchronize(EventBus eventBus) {
        if (eventBus instanceof SynchronizedEventBus) {
            return (SynchronizedEventBus) eventBus;
        }

        return new SynchronizedEventBus(eventBus);
    }

    private SynchronizedEventBus(final EventBus delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized Long getTime() {
        return delegate.getTime();
    }

    @Override
    public synchronized void send(final Event event) {
        super.send(event);
        delegate.send(event);
    }

    @Override
    public synchronized void sendAll(final Collection<Event> events) {
        super.sendAll(events);
    }

}
