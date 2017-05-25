package cucumber.api.event;

public interface EventHandler<T extends Event> {

    public void receive(T event);

}
