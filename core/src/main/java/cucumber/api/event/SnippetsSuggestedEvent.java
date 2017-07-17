package cucumber.api.event;

import gherkin.pickles.PickleLocation;

import java.util.Collections;
import java.util.List;

public class SnippetsSuggestedEvent extends TimeStampedEvent {
    public final String uri;
    public final List<PickleLocation> stepLocations;
    public final List<String> snippets;

    public SnippetsSuggestedEvent(Long timeStamp, String uri, List<PickleLocation> stepLocations, List<String> snippets) {
        super(timeStamp);
        this.uri = uri;
        this.stepLocations = stepLocations;
        this.snippets = Collections.unmodifiableList(snippets);
    }

}
