package io.cucumber.compatibility.undefined;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;

public class Undefined {

    @Given("an implemented step")
    public void anImplementedStep() {
    }

    @And("a step that will be skipped")
    public void aStepThatWillBeSkipped() {
    }
}
