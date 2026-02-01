package io.cucumber.compatibility.allstatuses;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

public final class AllStatuses {

    @Given("^a step$")
    public void a_step() {
        /* no-op */
    }

    @Given("^a failing step$")
    public void a_failing_step() {
        Assertions.fail();
    }

    @Given("^a pending step$")
    public void a_pending_step() {
        throw new PendingException();
    }

    @Given("^a skipped step$")
    public void a_skipped_step() {
        Assumptions.abort();
    }

    @Given("^an ambiguous (.*?)$")
    public void second_ambiguous_step(String a) {

    }

    @Given("^(.*?) ambiguous step$")
    public void first_ambiguous_step(String a) {

    }

}
