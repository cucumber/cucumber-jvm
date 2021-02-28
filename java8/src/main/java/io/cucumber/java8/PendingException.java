package io.cucumber.java8;

import io.cucumber.core.backend.Pending;
import org.apiguardian.api.API;

/**
 * When thrown from a step marks it as not yet implemented.
 *
 * @see Java8Snippet
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
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
