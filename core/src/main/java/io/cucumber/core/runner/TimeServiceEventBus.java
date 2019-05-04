package io.cucumber.core.runner;

import java.time.Instant;

import io.cucumber.core.event.AbstractEventBus;

public final class TimeServiceEventBus extends AbstractEventBus {
    private final TimeService stopWatch;

    public TimeServiceEventBus(TimeService stopWatch) {
        this.stopWatch = stopWatch;
    }

    @Override
    public Instant getTimeInstant() {
        return stopWatch.timeInstant();
    }
}
