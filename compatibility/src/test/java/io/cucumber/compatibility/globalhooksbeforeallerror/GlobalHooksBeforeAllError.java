package io.cucumber.compatibility.globalhooksbeforeallerror;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.When;

public final class GlobalHooksBeforeAllError {

    @BeforeAll(order = 1)
    public static void beforeAll1() {
    }

    @BeforeAll(order = 2)
    public static void beforeAll2() throws Exception {
        throw new Exception("beforeAll hook went wrong");
    }

    @BeforeAll(order = 3)
    public static void beforeAll3() {
    }

    @When("a step passes")
    public void aStepPasses() {
    }

    @AfterAll(order = 2)
    public static void afterAll2() {

    }

    @AfterAll(order = 1)
    public static void afterAll1() {

    }
}
