package io.cucumber.compatibility.skipped;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.junit.jupiter.api.Assumptions;

public class Skipped {

    @Before("@skip")
    public void before() {
        Assumptions.abort();
    }

    @Given("a step that is skipped")
    public void aStepThatIsSkipped() {
    }

    @Given("a step that does not skip")
    public void aStepThatDoesNotSkip() {
    }

    @And("I skip a step")
    public void iSkipAStep() {
        Assumptions.abort();
    }
}
