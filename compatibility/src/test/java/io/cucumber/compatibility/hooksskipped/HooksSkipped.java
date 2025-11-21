package io.cucumber.compatibility.hooksskipped;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.When;
import org.opentest4j.TestAbortedException;

public class HooksSkipped {

    @Before(order = 1)
    public void before1() {
    }

    @Before(value = "@skip-before", order = 2)
    public void beforeSkipped() {
        throw new TestAbortedException("skipped");
    }

    @Before(order = 3)
    public void before2() {
    }

    @When("a normal step")
    public void aStepPasses() {
    }

    @When("a step that skips")
    public void aStepFails() {
        throw new TestAbortedException("skipped");
    }

    @After(order = 1)
    public void after1() {
    }

    @After(value = "@skip-after", order = 2)
    public void afterSkipped() {
        throw new TestAbortedException("skipped");
    }

    @After(order = 3)
    public void after2() {
    }
}
