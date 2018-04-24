package cucumber.api.event;

public class TestGroupRunFinished extends TimeStampedEvent {

    private final String type;

    public TestGroupRunFinished(final String type, final Long timeStamp) {
        super(timeStamp);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
