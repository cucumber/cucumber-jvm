package cucumber.api.event;

import io.cucumber.messages.Messages.Location;

import java.util.Collections;
import java.util.List;

public class SnippetsSuggestedEvent extends TimeStampedEvent {
    public final String uri;
    public final List<Location> stepLocations;
    public final List<String> snippets;

    public SnippetsSuggestedEvent(Long timeStamp, String uri, List<Location> stepLocations, List<String> snippets) {
        super(timeStamp);
        this.uri = uri;
        this.stepLocations = stepLocations;
        this.snippets = Collections.unmodifiableList(snippets);
    }

}
