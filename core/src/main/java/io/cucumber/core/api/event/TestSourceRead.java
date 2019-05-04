package io.cucumber.core.api.event;

import java.time.Instant;

public final class TestSourceRead extends TimeStampedEvent {
    public final String uri;
    public final String source;

    @Deprecated
    public TestSourceRead(Long timeStamp, String uri, String source) {
        this(timeStamp, 0, uri, source);
    }

    @Deprecated
    public TestSourceRead(Long timeStamp, long timeStampMillis, String uri, String source) {
        super(timeStamp, timeStampMillis);
        this.uri = uri;
        this.source = source;
    }
    
    
    public TestSourceRead(Instant timeInstant, String uri, String source) {
        super(timeInstant);
        this.uri = uri;
        this.source = source;
    }

}
