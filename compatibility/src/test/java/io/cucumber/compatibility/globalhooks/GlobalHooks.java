package io.cucumber.compatibility.globalhooks;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.When;

public final class GlobalHooks {

    @BeforeAll
    public static void beforeAll1() {
    }

    @BeforeAll
    public static void beforeAll2() {
    }

    @When("a step passes")
    public void aStepPasses() {
    }

    @When("a step fails")
    public void aStepFails() throws Exception {
        throw new Exception("Exception in step");
    }

    @AfterAll
    public static void afterAll1() {

    }

    @AfterAll
    public static void afterAll2() {

    }
}
