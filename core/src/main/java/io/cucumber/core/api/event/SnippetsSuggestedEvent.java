package io.cucumber.core.api.event;

import gherkin.pickles.PickleLocation;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class SnippetsSuggestedEvent extends TimeStampedEvent {
    public final String uri;
    public final List<PickleLocation> stepLocations;
    public final List<String> snippets;

    public SnippetsSuggestedEvent(Instant timeInstant, String uri, List<PickleLocation> stepLocations, List<String> snippets) {
        super(timeInstant);
        this.uri = uri;
        this.stepLocations = stepLocations;
        this.snippets = Collections.unmodifiableList(snippets);
    }

}
