package cucumber.runner;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadLocalHandlersEventBus extends DefaultEventBus {

    private ThreadLocal<Map<Class<? extends Event>, List<EventHandler>>> handlers = new ThreadLocal<Map<Class<? extends Event>, List<EventHandler>>>() {
        @Override
        protected Map<Class<? extends Event>, List<EventHandler>> initialValue() {
            return new HashMap<Class<? extends Event>, List<EventHandler>>();
        }
    };

    public ThreadLocalHandlersEventBus(final TimeService stopWatch) {
        super(stopWatch);
    }

    @Override
    protected Map<Class<? extends Event>, List<EventHandler>> getHandlers() {
        return this.handlers.get();
    }
}
