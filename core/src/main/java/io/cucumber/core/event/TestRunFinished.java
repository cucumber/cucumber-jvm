package io.cucumber.core.event;

import java.time.Instant;

public final class TestRunFinished extends TimeStampedEvent {

    public TestRunFinished(Instant timeInstant) {
        super(timeInstant);
    }
}
