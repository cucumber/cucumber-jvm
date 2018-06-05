package cucumber.runtime.model;

import cucumber.api.event.TestSourceRead;
import cucumber.runner.EventBus;
import gherkin.ast.GherkinDocument;

import java.io.Serializable;
import java.util.Comparator;

public class CucumberFeature implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String uri;
    private GherkinDocument gherkinDocument;
    private String gherkinSource;


    public CucumberFeature(GherkinDocument gherkinDocument, String uri, String gherkinSource) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
    }

    public GherkinDocument getGherkinFeature() {
        return gherkinDocument;
    }

    public String getUri() {
        return uri;
    }

    public void sendTestSourceRead(EventBus bus) {
        bus.send(new TestSourceRead(bus.getTime(), uri, gherkinSource));
    }

    public static class CucumberFeatureUriComparator implements Comparator<CucumberFeature> {
        @Override
        public int compare(CucumberFeature a, CucumberFeature b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
