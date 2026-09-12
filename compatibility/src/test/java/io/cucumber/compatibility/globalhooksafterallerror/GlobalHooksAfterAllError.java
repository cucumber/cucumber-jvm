package io.cucumber.compatibility.globalhooksafterallerror;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.When;

public final class GlobalHooksAfterAllError {

    @BeforeAll(order = 1)
    public static void beforeAll1() {
    }

    @BeforeAll(order = 2)
    public static void beforeAll2() {
    }

    @When("a step passes")
    public void aStepPasses() {
    }

    @AfterAll(order = 3)
    public static void afterAll3() {

    }

    @AfterAll(order = 2)
    public static void afterAll2() throws Exception {
        throw new Exception("AfterAll hook went wrong");
    }

    @AfterAll(order = 1)
    public static void afterAll1() {

    }
}
