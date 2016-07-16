package cucumber.api.event;

public class WriteEvent implements Event {
    public final String text;

    public WriteEvent(String text) {
        this.text = text;
    }
}
