package io.cucumber.compatibility.globalhooksbeforeallerror;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.When;

public final class GlobalHooksBeforeAllError {

    @BeforeAll
    public static void beforeAll1() {
    }

    @BeforeAll
    public static void beforeAll2() throws Exception {
        throw new Exception("AfterAll hook went wrong");
    }

    @BeforeAll
    public static void beforeAll3() {
    }

    @When("a step passes")
    public void aStepPasses() {
    }

    @AfterAll
    public static void afterAll1() {

    }

    @AfterAll
    public static void afterAll2() {
    }
}
