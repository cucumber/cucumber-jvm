package io.cucumber.core.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@API(status = API.Status.STABLE)
public final class SnippetsSuggestedEvent extends TimeStampedEvent {
    private final String uri;
    private final List<Location> stepLocations;
    private final List<String> snippets;

    public SnippetsSuggestedEvent(Instant timeInstant, String uri, List<Location> stepLocations, List<String> snippets) {
        super(timeInstant);
        this.uri = Objects.requireNonNull(uri);
        this.stepLocations = Objects.requireNonNull(stepLocations);
        this.snippets = Collections.unmodifiableList(Objects.requireNonNull(snippets));
    }

    public String getUri() {
        return uri;
    }

    public List<Location> getStepLocations() {
        return stepLocations;
    }

    public List<String> getSnippets() {
        return snippets;
    }

    public static final class Location {
        private final int line;
        private final int column;

        public Location(int line, int column) {
            this.line = line;
            this.column = column;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }
    }

}
