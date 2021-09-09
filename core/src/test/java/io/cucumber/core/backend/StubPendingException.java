package io.cucumber.core.backend;

import io.cucumber.core.backend.Pending;

@Pending
public final class StubPendingException extends RuntimeException {

    public StubPendingException() {
        this("TODO: implement me");
    }

    public StubPendingException(String message) {
        super(message);
    }

}
