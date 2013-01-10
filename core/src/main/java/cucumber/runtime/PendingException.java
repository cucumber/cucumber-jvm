package cucumber.runtime;

import cucumber.api.Pending;

/**
 * @see cucumber.api.PendingException
 * @deprecated Use cucumber.api.PendingException
 */
@Deprecated
@Pending
public class PendingException extends RuntimeException {
    public PendingException() {
        this("TODO: implement me");
    }

    public PendingException(String message) {
        super(message);
    }
}
