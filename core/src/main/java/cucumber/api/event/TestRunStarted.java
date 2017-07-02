package cucumber.api.event;

public final class TestRunStarted extends TimeStampedEvent {

    public TestRunStarted(Long timeStamp) {
        super(timeStamp);
    }
}
