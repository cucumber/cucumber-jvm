package io.cucumber.core.runner;

import java.time.Duration;
import java.time.Instant;

public class TimeServiceStub implements TimeService {
    private final Duration duration;
    private final ThreadLocal<Instant> currentInstant = new ThreadLocal<>();

    public TimeServiceStub(Duration duration) {
        this.duration = duration;
    }
    
    @Override
    public Instant timeInstant() {
        Instant result = currentInstant.get();
        result = result != null ? result : Instant.EPOCH;
        currentInstant.set(result.plus(duration));
        return result;
    }
}
