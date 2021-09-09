package io.cucumber.compatibility.stacktraces;

import io.cucumber.java.en.When;

public class StackTraces {

    @When("a step throws an exception")
    public void test() throws Exception {
        throw new Exception("BOOM");
    }

}
