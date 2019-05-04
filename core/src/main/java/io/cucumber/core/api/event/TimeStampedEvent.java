package io.cucumber.core.api.event;

import java.time.Instant;

abstract class TimeStampedEvent implements Event {

    private final Long timeStamp;
    private final long timeStampMillis;
    private final Instant timeInstant;

    @Deprecated
    TimeStampedEvent(Long timeStamp, Long timeStampMillis) {
        this.timeStamp = timeStamp;
        this.timeStampMillis = timeStampMillis;
        this.timeInstant = Instant.EPOCH;
    }

    TimeStampedEvent(Instant timeInstant) {
        this.timeStamp = 0L;
        this.timeStampMillis = 0L;
        this.timeInstant = timeInstant;
    }
    
    /**
     * {@inheritDoc}
     */

    @Deprecated
    @Override
    public Long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns timestamp in milliseconds of the epoch.
     *
     * @return timestamp in milli seconds
     * @see System#currentTimeMillis()
     */
    
    @Deprecated
    public long getTimeStampMillis() {
        return timeStampMillis;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Instant getTimeInstant() {
        return timeInstant;
    }
}
