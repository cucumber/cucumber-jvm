package io.cucumber.compatibility.pendingexception;

import io.cucumber.java.en.Given;

public final class PendingException {

    @Given("an unimplemented pending step")
    public void anUnimplementedPendingStep() {
        throw new io.cucumber.java.PendingException("TODO");
    }

}
