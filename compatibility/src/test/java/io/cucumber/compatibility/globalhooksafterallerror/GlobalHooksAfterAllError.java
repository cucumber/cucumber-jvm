package io.cucumber.compatibility.globalhooksafterallerror;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.When;

public final class GlobalHooksAfterAllError {

    @BeforeAll
    public static void beforeAll1() {
    }

    @BeforeAll
    public static void beforeAll2() {
    }

    @When("a step passes")
    public void aStepPasses() {
    }

    @AfterAll
    public static void afterAll1() {

    }

    @AfterAll
    public static void afterAll2() throws Exception {
        throw new Exception("AfterAll hook went wrong");
    }

    @AfterAll
    public static void afterAll3() {

    }
}
