package io.cucumber.junit.api;

import io.cucumber.core.backend.Pending;

@Pending
public final class TestPendingException extends RuntimeException {
    public TestPendingException() {
        this("TODO: implement me");
    }

    public TestPendingException(String message) {
        super(message);
    }
}
