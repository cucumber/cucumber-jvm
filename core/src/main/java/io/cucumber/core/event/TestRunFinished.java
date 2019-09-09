package io.cucumber.core.event;

import org.apiguardian.api.API;

import java.time.Instant;

@API(status = API.Status.STABLE)
public final class TestRunFinished extends TimeStampedEvent {

    public TestRunFinished(Instant timeInstant) {
        super(timeInstant);
    }
}
