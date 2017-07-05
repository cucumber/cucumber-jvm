package cucumber.api.event;

public interface EventPublisher {
    <T extends Event> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler);
}
