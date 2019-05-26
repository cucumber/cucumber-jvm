package io.cucumber.core.api.event;

import java.time.Instant;

public final class TestRunFinished extends TimeStampedEvent {

    public TestRunFinished(Instant timeInstant) {
        super(timeInstant);
    }
}
