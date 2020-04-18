package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;

@API(status = API.Status.STABLE)
public final class TestRunFinished extends TimeStampedEvent {

    private final Exception exception;

    public TestRunFinished(Instant timeInstant) {
        this(timeInstant, null);
    }

    public TestRunFinished(Instant timeInstant, Exception exception) {
        super(timeInstant);
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
