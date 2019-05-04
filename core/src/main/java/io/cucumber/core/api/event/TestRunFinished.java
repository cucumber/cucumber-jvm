package io.cucumber.core.api.event;

import java.time.Instant;

public final class TestRunFinished extends TimeStampedEvent {

    @Deprecated
    public TestRunFinished(Long timeStamp) {
        this(timeStamp, 0);
    }

    @Deprecated
    public TestRunFinished(Long timeStamp, long timeStampMillis) {
        super(timeStamp, timeStampMillis);
    }

    public TestRunFinished(Instant timeInstant) {
        super(timeInstant);
    }
}
