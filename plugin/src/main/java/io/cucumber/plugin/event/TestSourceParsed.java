package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@API(status = API.Status.EXPERIMENTAL)
public final class TestSourceParsed extends TimeStampedEvent {
    private final URI uri;
    private final List<Node> nodes;

    public TestSourceParsed(Instant timeInstant, URI uri, List<Node> nodes) {
        super(timeInstant);
        this.uri = Objects.requireNonNull(uri);
        this.nodes = Objects.requireNonNull(nodes);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public URI getUri() {
        return uri;
    }
}
