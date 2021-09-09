package io.cucumber.core.runner;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class ClockStub extends Clock {

    private final Duration duration;
    private final ThreadLocal<Instant> currentInstant = new ThreadLocal<>();

    public ClockStub(Duration duration) {
        this.duration = duration;
    }

    @Override
    public ZoneId getZone() {
        return null;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return null;
    }

    @Override
    public Instant instant() {
        Instant result = currentInstant.get();
        result = result != null ? result : Instant.EPOCH;
        currentInstant.set(result.plus(duration));
        return result;
    }

}
