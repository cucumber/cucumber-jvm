package cucumber;

import gherkin.formatter.Formatter;

public class FeatureSource {
    private final String source;
    private final String uri;

    public FeatureSource(String source, String uri) {
        this.source = source;
        this.uri = uri;
    }

    public void execute(Runtime runtime) {
        runtime.execute(source, uri);
    }
}
