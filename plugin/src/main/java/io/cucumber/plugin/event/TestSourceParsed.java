package io.cucumber.plugin.event;

import io.cucumber.plugin.event.Node.Container;
import org.apiguardian.api.API;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.EXPERIMENTAL)
public final class TestSourceParsed extends TimeStampedEvent {
    private final URI uri;
    private final Container<Node> container;

    public TestSourceParsed(Instant timeInstant, URI uri, Container<Node> container) {
        super(timeInstant);
        this.uri = Objects.requireNonNull(uri);
        this.container = Objects.requireNonNull(container);
    }

    public Container<Node> getContainer() {
        return container;
    }

    public URI getUri() {
        return uri;
    }
}
