package io.cucumber.java;

import io.cucumber.core.backend.Pending;
import org.apiguardian.api.API;

/**
 * When thrown from a step marks it as not yet implemented.
 *
 * @see JavaSnippet
 */
@Pending
@API(status = API.Status.STABLE)
public final class PendingException extends RuntimeException {

    public PendingException() {
        this("TODO: implement me");
    }

    public PendingException(String message) {
        super(message);
    }

}
