package io.cucumber.core.event;

import java.time.Instant;

public final class TestRunStarted extends TimeStampedEvent {

    public TestRunStarted(Instant timeInstant) {
        super(timeInstant);
    }
}
