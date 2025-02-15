package io.cucumber.compatibility.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.When;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Hooks {

    @Before
    public void before() {
    }

    @When("a step passes")
    public void aStepPasses() {
    }

    @When("a step fails")
    public void aStepFails() throws Exception {
        throw new Exception("Exception in step");
    }

    @After
    public void after() throws Exception {

    }
}
