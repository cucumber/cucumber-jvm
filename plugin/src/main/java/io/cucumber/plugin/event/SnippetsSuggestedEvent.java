package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

@API(status = API.Status.STABLE)
public final class SnippetsSuggestedEvent extends TimeStampedEvent {
    private final URI uri;
    private final int scenarioLine;
    private final int stepLine;
    private final List<String> snippets;

    public SnippetsSuggestedEvent(Instant timeInstant, URI uri, int scenarioLine, int stepLine, List<String> snippets) {
        super(timeInstant);
        this.uri = requireNonNull(uri);
        this.scenarioLine = scenarioLine;
        this.stepLine = stepLine;
        this.snippets = unmodifiableList(requireNonNull(snippets));
    }

    public URI getUri() {
        return uri;
    }

    public int getStepLine() {
        return stepLine;
    }

    public int getScenarioLine() {
        return scenarioLine;
    }

    public List<String> getSnippets() {
        return snippets;
    }

}
