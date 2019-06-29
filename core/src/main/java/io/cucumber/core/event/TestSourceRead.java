package io.cucumber.core.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.STABLE)
public final class TestSourceRead extends TimeStampedEvent {
    private final String uri;
    private final String source;

    public TestSourceRead(Instant timeInstant, String uri, String source) {
        super(timeInstant);
        this.uri = Objects.requireNonNull(uri);
        this.source = Objects.requireNonNull(source);
    }

    public String getSource() {
        return source;
    }

    public String getUri() {
        return uri;
    }
}
