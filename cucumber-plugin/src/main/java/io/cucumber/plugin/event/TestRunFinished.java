package io.cucumber.plugin.event;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.STABLE)
public final class TestRunFinished extends TimeStampedEvent {

    private final @Nullable Result result;

    @Deprecated
    public TestRunFinished(Instant timeInstant) {
        super(timeInstant);
        this.result = null;
    }

    public TestRunFinished(Instant timeInstant, Result result) {
        super(timeInstant);
        this.result = Objects.requireNonNull(result);
    }

    public @Nullable Result getResult() {
        return result;
    }

}
