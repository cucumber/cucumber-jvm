package io.cucumber.core.api.event;

import java.time.Instant;

public final class TestSourceRead extends TimeStampedEvent {
    public final String uri;
    public final String source;

    public TestSourceRead(Instant timeInstant, String uri, String source) {
        super(timeInstant);
        this.uri = uri;
        this.source = source;
    }

}
