package io.cucumber.core.event;

import java.time.Instant;

abstract class TimeStampedEvent implements Event {

    private final Instant instant;

    TimeStampedEvent(Instant timeInstant) {
        this.instant = timeInstant;
    }
    
    /**
     * {@inheritDoc}
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant getInstant() {
        return instant;
    }
}
