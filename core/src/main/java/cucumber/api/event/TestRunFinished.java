package cucumber.api.event;

public class TestRunFinished extends TimeStampedEvent {

    public TestRunFinished(Long timeStamp) {
        super(timeStamp);
    }
}
