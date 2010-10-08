package cucumber;

import cucumber.runtime.Executor;
import gherkin.FeatureParser;

public class FeatureSource {
    private final String source;
    private final String uri;

    public FeatureSource(String source, String uri) {
        this.source = source;
        this.uri = uri;
    }

    public void execute(FeatureParser parser) {
        parser.parse(source, uri, 0);
    }
}
