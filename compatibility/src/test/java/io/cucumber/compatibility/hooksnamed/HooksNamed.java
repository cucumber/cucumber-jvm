package io.cucumber.compatibility.hooksnamed;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.When;

public final class HooksNamed {

    @Before(name = "A named before hook")
    public void before() {
    }

    @When("a step passes")
    public void aStepPasses() {
    }

    @After(name = "A named after hook")
    public void after() {

    }
}
