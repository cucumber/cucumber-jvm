package cucumber.runtime.model;

import cucumber.api.event.TestSourceRead;
import io.cucumber.messages.Messages.GherkinDocument;
import cucumber.runner.EventBus;

import java.io.Serializable;
import java.util.Comparator;

public class CucumberFeature implements Serializable {
    private static final long serialVersionUID = 1L;
    private GherkinDocument gherkinDocument;
    private String gherkinSource;


    public CucumberFeature(GherkinDocument gherkinDocument, String gherkinSource) {
        this.gherkinDocument = gherkinDocument;
        this.gherkinSource = gherkinSource;
    }

    public GherkinDocument getGherkinFeature() {
        return gherkinDocument;
    }

    public String getUri() {
        return gherkinDocument.getUri();
    }

    public void sendTestSourceRead(EventBus bus) {
        bus.send(new TestSourceRead(bus.getTime(), gherkinSource));
    }

    public static class CucumberFeatureUriComparator implements Comparator<CucumberFeature> {
        @Override
        public int compare(CucumberFeature a, CucumberFeature b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
