package cucumber.api.event;

public class TestSourceRead implements Event {
    public final String path;
    public final String language;
    public final String source;

    public TestSourceRead(String path, String language, String source) {
        this.path = path;
        this.language = language;
        this.source = source;
    }

}
