package io.cucumber.java.custompredicateclass;

import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.fail;

public class CustomPredicateClassSteps {

    @Then("run a step bound to fail")
    public void run_a_step_bound_to_fail() {
        fail("This scenario should have been filtered out by the customPredicateClass");
    }
}
