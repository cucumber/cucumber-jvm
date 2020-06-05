package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.STABLE)
public final class TestSourceRead extends TimeStampedEvent {

    private final URI uri;
    private final String source;

    public TestSourceRead(Instant timeInstant, URI uri, String source) {
        super(timeInstant);
        this.uri = Objects.requireNonNull(uri);
        this.source = Objects.requireNonNull(source);
    }

    public String getSource() {
        return source;
    }

    public URI getUri() {
        return uri;
    }

}
