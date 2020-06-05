package io.cucumber.plugin.event;

import java.time.Instant;
import java.util.Objects;

abstract class TimeStampedEvent implements Event {

    private final Instant instant;

    TimeStampedEvent(Instant timeInstant) {
        this.instant = Objects.requireNonNull(timeInstant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant getInstant() {
        return instant;
    }

}
