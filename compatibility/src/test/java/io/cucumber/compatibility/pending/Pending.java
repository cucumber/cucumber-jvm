package io.cucumber.compatibility.pending;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Pending {

    @Given("an unimplemented pending step")
    public void anUnimplementedPendingStep() {
        throw new PendingException("Not yet implemented");
    }

    @Given("an implemented non-pending step")
    public void anImplementedNonPendingStep() {
    }

    @And("an implemented step that is skipped")
    public void anImplementedStepThatIsSkipped() {
    }
}
