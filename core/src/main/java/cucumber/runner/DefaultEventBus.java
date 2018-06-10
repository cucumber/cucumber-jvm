package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultEventBus implements EventBus {
    private final TimeService stopWatch;
    private Map<Class<? extends Event>, List<EventHandler>> handlers = new HashMap<Class<? extends Event>, List<EventHandler>>();

    public DefaultEventBus(TimeService stopWatch) {
        this.stopWatch = stopWatch;
    }

    @Override
    public Long getTime() {
        return stopWatch.time();
    }

    @Override
    public void send(Event event) {
        if (getHandlers().containsKey(event.getClass())) {
            for (EventHandler handler : getHandlers().get(event.getClass())) {
                //noinspection unchecked: protected by registerHandlerFor
                handler.receive(event);
            }
        }
    }

    @Override
    public <T extends Event> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (getHandlers().containsKey(eventType)) {
            getHandlers().get(eventType).add(handler);
        } else {
            List<EventHandler> list = new ArrayList<EventHandler>();
            list.add(handler);
            getHandlers().put(eventType, list);
        }
    }

    @Override
    public <T extends Event> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (getHandlers().containsKey(eventType)) {
            getHandlers().get(eventType).remove(handler);
        }
    }

    protected Map<Class<? extends Event>, List<EventHandler>> getHandlers() {
        return this.handlers;
    }
}
