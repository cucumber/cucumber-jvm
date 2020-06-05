package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;

@API(status = API.Status.STABLE)
public final class TestRunStarted extends TimeStampedEvent {

    public TestRunStarted(Instant timeInstant) {
        super(timeInstant);
    }

}
