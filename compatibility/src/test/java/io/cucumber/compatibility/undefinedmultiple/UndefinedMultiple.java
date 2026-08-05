package io.cucumber.compatibility.undefinedmultiple;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;

public final class UndefinedMultiple {

    @Given("an implemented step")
    public void anImplementedStep() {
    }

    @And("a step that will be skipped")
    public void aStepThatWillBeSkipped() {
    }
}
