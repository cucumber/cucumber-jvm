package cucumber.api.event;

public class TestGroupRunStarted extends TimeStampedEvent {

    private final String type;
    private final int threadCount;
    private final int featureCount;

    public TestGroupRunStarted(final String type, final int threadCount, final int featureCount, final Long timeStamp) {
        super(timeStamp);
        this.type = type;
        this.threadCount = threadCount;
        this.featureCount = featureCount;
    }

    public String getType() {
        return type;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getFeatureCount() {
        return featureCount;
    }
}
