package cucumber.api.event;

public class TestSourceRead extends TimeStampedEvent {
    public final String path;
    public final String language;
    public final String source;

    public TestSourceRead(Long timeStamp, String path, String language, String source) {
        super(timeStamp);
        this.path = path;
        this.language = language;
        this.source = source;
    }

}
