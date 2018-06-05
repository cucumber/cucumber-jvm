package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus implements EventPublisher {
    private final TimeService stopWatch;
    private Map<Class<? extends Event>, List<EventHandler>> handlers = new HashMap<Class<? extends Event>, List<EventHandler>>();

    public EventBus(TimeService stopWatch) {
        this.stopWatch = stopWatch;
    }

    public EventBus createBatchedEventBus() {
        return new BatchEventBus(this);
    }

    public Long getTime() {
        return stopWatch.time();
    }


    public synchronized void send(Event event) {
        if (handlers.containsKey(event.getClass())) {
            for (EventHandler handler : handlers.get(event.getClass())) {
                //noinspection unchecked: protected by registerHandlerFor
                handler.receive(event);
            }
        }
    }

    synchronized void sendAll(List<Event> events) {
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

    public <T extends Event> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (handlers.containsKey(eventType)) {
            handlers.get(eventType).remove(handler);
        }
    }


    private class BatchEventBus extends EventBus {

        private final EventBus parent;
        private final List<Event> queue = new ArrayList<Event>();

        BatchEventBus(EventBus parent) {
            super(parent.stopWatch);
            this.parent = parent;
        }

        @Override
        public void send(Event event) {
            super.send(event);
            queue.add(event);
            if(event instanceof TestCaseFinished){
                parent.sendAll(queue);
                queue.clear();
            }
        }
    }

}
