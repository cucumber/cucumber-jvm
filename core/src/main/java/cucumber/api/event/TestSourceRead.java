package cucumber.api.event;

public final class TestSourceRead extends TimeStampedEvent {
    public final String uri;
    public final String source;

    @Deprecated
    public TestSourceRead(Long timeStamp, String uri, String source) {
        this(timeStamp, 0, uri, source);
    }

    public TestSourceRead(Long timeStamp, long timeStampMillis, String uri, String source) {
        super(timeStamp, timeStampMillis);
        this.uri = uri;
        this.source = source;
    }

}
