package cucumber.runtime.model;

import cucumber.api.event.TestSourceRead;
import cucumber.runner.EventBus;
import gherkin.ast.GherkinDocument;
import gherkin.events.PickleEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CucumberFeature {
    private final String uri;
    private final List<PickleEvent> pickles;
    private GherkinDocument gherkinDocument;
    private String gherkinSource;


    public CucumberFeature(GherkinDocument gherkinDocument, String uri, String gherkinSource, List<PickleEvent> pickles) {
        this.gherkinDocument = gherkinDocument;
        this.uri = uri;
        this.gherkinSource = gherkinSource;
        this.pickles = pickles;
    }

    public List<PickleEvent> getPickles() {
        return pickles;
    }

    public String getName() {
        return gherkinDocument.getFeature().getName();
    }

    public GherkinDocument getGherkinFeature() {
        return gherkinDocument;
    }

    public String getUri() {
        return uri;
    }

    public void sendTestSourceRead(EventBus bus) {
        bus.send(new TestSourceRead(bus.getTime(), getUri(), gherkinSource));
    }

    String getSource() {
        return gherkinSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CucumberFeature that = (CucumberFeature) o;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    public static class CucumberFeatureUriComparator implements Comparator<CucumberFeature> {
        @Override
        public int compare(CucumberFeature a, CucumberFeature b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
