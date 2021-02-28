package io.cucumber.core.eventbus;

import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractEventPublisher implements EventPublisher {

    protected final Map<Class<?>, List<EventHandler>> handlers = new HashMap<>();

    @Override
    public final <T> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (handlers.containsKey(eventType)) {
            handlers.get(eventType).add(handler);
        } else {
            List<EventHandler> list = new ArrayList<>();
            list.add(handler);
            handlers.put(eventType, list);
        }
    }

    @Override
    public final <T> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (handlers.containsKey(eventType)) {
            handlers.get(eventType).remove(handler);
        }
    }

    protected <T> void sendAll(Iterable<T> events) {
        for (T event : events) {
            send(event);
        }
    }

    protected <T> void send(T event) {
        if (handlers.containsKey(Event.class) && event instanceof Event) {
            for (EventHandler handler : handlers.get(Event.class)) {
                // noinspection unchecked: protected by registerHandlerFor
                handler.receive(event);
            }
        }

        if (handlers.containsKey(event.getClass())) {
            for (EventHandler handler : handlers.get(event.getClass())) {
                // noinspection unchecked: protected by registerHandlerFor
                handler.receive(event);
            }
        }
    }

}
