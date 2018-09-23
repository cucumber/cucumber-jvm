package io.cucumber.java.api;

import io.cucumber.core.backend.Pending;

// We're deliberately not extending CucumberException (which is used to signal fatal errors)
@Pending
public final class PendingException extends RuntimeException {
    public PendingException() {
        this("TODO: implement me");
    }

    public PendingException(String message) {
        super(message);
    }
}
