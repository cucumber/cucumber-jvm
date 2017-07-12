package cucumber.api.event;

public final class TestSourceRead extends TimeStampedEvent {
    public final String uri;
    public final String language;
    public final String source;

    public TestSourceRead(Long timeStamp, String uri, String language, String source) {
        super(timeStamp);
        this.uri = uri;
        this.language = language;
        this.source = source;
    }

}
