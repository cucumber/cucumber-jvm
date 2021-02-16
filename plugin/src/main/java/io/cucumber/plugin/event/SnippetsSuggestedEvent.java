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
    private final Location testCaseLocation;
    private final Location stepLocation;
    private final Suggestion suggestion;

    @Deprecated
    public SnippetsSuggestedEvent(Instant timeInstant, URI uri, int scenarioLine, int stepLine, List<String> snippets) {
        this(timeInstant, uri, new Location(scenarioLine, -1), new Location(stepLine, -1), snippets);
    }

    @Deprecated
    public SnippetsSuggestedEvent(
            Instant instant, URI uri, Location testCaseLocation, Location stepLocation, List<String> snippets
    ) {
        this(instant, uri, testCaseLocation, stepLocation, new Suggestion("", snippets));
    }

    public SnippetsSuggestedEvent(
            Instant instant, URI uri, Location testCaseLocation, Location stepLocation, Suggestion suggestion
    ) {
        super(instant);
        this.uri = requireNonNull(uri);
        this.testCaseLocation = requireNonNull(testCaseLocation);
        this.stepLocation = requireNonNull(stepLocation);
        this.suggestion = requireNonNull(suggestion);
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
        return testCaseLocation.getLine();
    }

    @Deprecated
    public Location getScenarioLocation() {
        return testCaseLocation;
    }

    public Location getTestCaseLocation() {
        return testCaseLocation;
    }

    public Location getStepLocation() {
        return stepLocation;
    }

    @Deprecated
    public List<String> getSnippets() {
        return suggestion.getSnippets();
    }

    public Suggestion getSuggestion() {
        return suggestion;
    }

    public static final class Suggestion {

        final String step;
        final List<String> snippets;

        public Suggestion(String step, List<String> snippets) {
            this.step = requireNonNull(step);
            this.snippets = unmodifiableList(requireNonNull(snippets));
        }

        public String getStep() {
            return step;
        }

        public List<String> getSnippets() {
            return snippets;
        }

    }
}
