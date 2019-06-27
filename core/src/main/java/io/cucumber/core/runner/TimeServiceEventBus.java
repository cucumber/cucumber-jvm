package io.cucumber.core.runner;

import java.time.Clock;
import java.time.Instant;

import io.cucumber.core.eventbus.AbstractEventBus;

public final class TimeServiceEventBus extends AbstractEventBus {
    private final Clock clock;

    public TimeServiceEventBus(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant getInstant() {
        return clock.instant();
    }
}
