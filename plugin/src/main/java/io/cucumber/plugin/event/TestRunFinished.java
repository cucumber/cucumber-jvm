package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;

@API(status = API.Status.STABLE)
public final class TestRunFinished extends TimeStampedEvent {

    private final Result result;

    public TestRunFinished(Instant timeInstant) {
        this(timeInstant, null);
    }

    public TestRunFinished(Instant timeInstant, Result result) {
        super(timeInstant);
        this.result = result;
    }

    public Result getResult() {
        return result;
    }
}
