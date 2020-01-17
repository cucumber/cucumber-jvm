package io.cucumber.compatibility.hooks;

import io.cucumber.java.en.When;

public class Hooks {

    @When("a step passes")
    public void aStepPasses() {
    }

    @When("a step throws an exception")
    public void test() throws Exception {
        throw new Exception("Boom");
    }
}
