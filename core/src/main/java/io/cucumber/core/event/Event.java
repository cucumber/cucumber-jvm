package io.cucumber.core.event;

import org.apiguardian.api.API;

import java.time.Instant;

@API(status = API.Status.STABLE)
public interface Event {

    /**
     * Returns instant from epoch.
     *
     * @return time instant in Instant
     * @see Instant#now()
     */
    Instant getInstant();
}
