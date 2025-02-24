package io.cucumber.compatibility.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.When;

public class Hooks {

    @Before
    public void before() {
    }

    @When("a step passes")
    public void aStepPasses() {
    }

    @When("a step fails")
    public void aStepFails() throws Exception {
        throw new Exception("Exception in step");
    }

    @After
    public void after() throws Exception {

    }
}
