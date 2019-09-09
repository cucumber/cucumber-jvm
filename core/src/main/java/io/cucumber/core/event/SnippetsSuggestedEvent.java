package io.cucumber.core.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

@API(status = API.Status.STABLE)
public final class SnippetsSuggestedEvent extends TimeStampedEvent {
    private final String uri;
    private final int stepLine;
    private final List<String> snippets;

    public SnippetsSuggestedEvent(Instant timeInstant, String uri, int stepLine, List<String> snippets) {
        super(timeInstant);
        this.uri = requireNonNull(uri);
        this.stepLine = stepLine;
        this.snippets = unmodifiableList(requireNonNull(snippets));
    }

    public String getUri() {
        return uri;
    }

    public int getStepLine() {
        return stepLine;
    }

    public List<String> getSnippets() {
        return snippets;
    }

}
