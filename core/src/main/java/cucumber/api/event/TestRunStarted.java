package cucumber.api.event;

public final class TestRunStarted extends TimeStampedEvent {

    @Deprecated
    public TestRunStarted(Long timeStamp) {
        this(timeStamp, 0);
    }

    public TestRunStarted(Long timeStamp, long timeStampMillis) {
        super(timeStamp, timeStampMillis);
    }
}
