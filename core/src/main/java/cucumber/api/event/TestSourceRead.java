package cucumber.api.event;

public final class TestSourceRead extends TimeStampedEvent {
    public final String source;

    public TestSourceRead(Long timeStamp, String source) {
        super(timeStamp);
        this.source = source;
    }

}
