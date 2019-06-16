package io.cucumber.testng;

import cucumber.api.Pending;

@Pending
public final class TestPendingException extends RuntimeException {
    public TestPendingException() {
        this("TODO: implement me");
    }

    public TestPendingException(String message) {
        super(message);
    }
}
