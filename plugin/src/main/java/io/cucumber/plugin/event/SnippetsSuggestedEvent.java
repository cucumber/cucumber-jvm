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
    private final Location scenarioLocation;
    private final Location stepLocation;
    private final List<String> snippets;

    @Deprecated
    public SnippetsSuggestedEvent(Instant timeInstant, URI uri, int scenarioLine, int stepLine, List<String> snippets) {
        this(timeInstant, uri, new Location(scenarioLine, -1), new Location(stepLine, -1), snippets);
    }

    public SnippetsSuggestedEvent(
            Instant instant, URI uri, Location scenarioLocation, Location stepLocation, List<String> snippets
    ) {
        super(instant);
        this.uri = requireNonNull(uri);
        this.scenarioLocation = scenarioLocation;
        this.stepLocation = stepLocation;
        this.snippets = unmodifiableList(requireNonNull(snippets));
    }

    public URI getUri() {
        return uri;
    }

    @Deprecated
    public int getStepLine() {
        return stepLocation.getLine();
    }

    @Deprecated
    public int getScenarioLine() {
        return scenarioLocation.getLine();
    }

    public Location getScenarioLocation() {
        return scenarioLocation;
    }

    public Location getStepLocation() {
        return stepLocation;
    }

    public List<String> getSnippets() {
        return snippets;
    }

}
