package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractEventBus implements EventBus {
    private Map<Class<? extends Event>, List<EventHandler>> handlers = new HashMap<Class<? extends Event>, List<EventHandler>>();

    @Override
    public void send(Event event) {
        if (handlers.containsKey(event.getClass())) {
            for (EventHandler handler : handlers.get(event.getClass())) {
                //noinspection unchecked: protected by registerHandlerFor
                handler.receive(event);
            }
        }
    }

    @Override
    public void sendAll(Collection<Event> events) {
        for (Event event : events) {
            send(event);
        }
    }

    @Override
    public <T extends Event> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (handlers.containsKey(eventType)) {
            handlers.get(eventType).add(handler);
        } else {
            List<EventHandler> list = new ArrayList<EventHandler>();
            list.add(handler);
            handlers.put(eventType, list);
        }
    }

    @Override
    public <T extends Event> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (handlers.containsKey(eventType)) {
            handlers.get(eventType).remove(handler);
        }
    }

}
