package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.AbstractEventBus;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

public final class TimeServiceEventBus extends AbstractEventBus {

    private final Clock clock;
    private final Supplier<UUID> idGenerator;

    public TimeServiceEventBus(Clock clock, Supplier<UUID> idGenerator) {
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    @Override
    public Instant getInstant() {
        return clock.instant();
    }

    @Override
    public UUID generateId() {
        return idGenerator.get();
    }

}
