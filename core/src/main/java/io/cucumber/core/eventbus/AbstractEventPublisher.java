package io.cucumber.core.eventbus;

import io.cucumber.core.event.Event;
import io.cucumber.core.event.EventHandler;
import io.cucumber.core.event.EventPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractEventPublisher implements EventPublisher {
    protected final Map<Class<? extends Event>, List<EventHandler>> handlers = new HashMap<>();

    @Override
    public final <T extends Event> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (handlers.containsKey(eventType)) {
            handlers.get(eventType).add(handler);
        } else {
            List<EventHandler> list = new ArrayList<>();
            list.add(handler);
            handlers.put(eventType, list);
        }
    }

    @Override
    public final <T extends Event> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (handlers.containsKey(eventType)) {
            handlers.get(eventType).remove(handler);
        }
    }


    protected void send(Event event) {
        if (handlers.containsKey(Event.class)) {
            for (EventHandler handler : handlers.get(Event.class)) {
                //noinspection unchecked: protected by registerHandlerFor
                handler.receive(event);
            }
        }

        if (handlers.containsKey(event.getClass())) {
            for (EventHandler handler : handlers.get(event.getClass())) {
                //noinspection unchecked: protected by registerHandlerFor
                handler.receive(event);
            }
        }
    }

    protected void sendAll(Iterable<Event> events) {
        for (Event event : events) {
            send(event);
        }
    }
}
