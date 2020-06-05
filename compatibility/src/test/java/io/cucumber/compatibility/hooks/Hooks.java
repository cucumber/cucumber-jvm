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

    @When("a step throws an exception")
    public void test() throws Exception {
        throw new Exception("Boom");
    }

    @After
    public void afterWithException() throws Exception {
        throw new Exception("Exception in hook");
    }

    @After("@some-tag or @some-other-tag")
    public void taggedAfterWithException() throws Exception {
        throw new Exception("Exception in conditional hook");
    }

}
