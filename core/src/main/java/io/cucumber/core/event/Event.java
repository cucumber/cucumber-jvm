package io.cucumber.core.event;

import java.time.Instant;
public interface Event {

    /**
     * Returns instant from epoch.
     *
     * @return time instant in Instant
     * @see Instant#now()
     */
    Instant getInstant();
}
