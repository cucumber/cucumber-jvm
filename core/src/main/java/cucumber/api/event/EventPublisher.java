package cucumber.api.event;

public interface EventPublisher {
    void registerHandlerFor(Class<? extends Event> eventType, EventHandler<? extends Event> handler);
}
