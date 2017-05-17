package cucumber.api.event;

public final class TestRunFinished extends TimeStampedEvent {

    public TestRunFinished(Long timeStamp) {
        super(timeStamp);
    }
}
