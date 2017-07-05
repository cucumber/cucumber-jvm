package cucumber.api.event;

public final class WriteEvent extends TimeStampedEvent {
    public final String text;

    public WriteEvent(Long timeStamp, String text) {
        super(timeStamp);
        this.text = text;
    }
}
