package io.cucumber.compatibility.hooksconditional;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.When;

public class HooksConditional {

    @Before("@passing-hook")
    public void before() {
    }

    @Before("@fail-before")
    public void failBefore() throws Exception {
        throw new Exception("Exception in conditional hook");
    }

    @When("a step passes")
    public void aStepPasses() {
    }

    @After("@fail-after")
    public void failAfter() throws Exception {
        throw new Exception("Exception in conditional hook");
    }

    @After("@passing-hook")
    public void after() throws Exception {
    }

}
