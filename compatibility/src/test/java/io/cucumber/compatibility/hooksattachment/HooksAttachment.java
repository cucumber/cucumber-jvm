package io.cucumber.compatibility.hooksattachment;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.When;

import java.io.IOException;

import static io.cucumber.compatibility.Resources.read;

public final class HooksAttachment {

    @Before
    public void before(Scenario scenario) throws IOException {
        attachImage(scenario);
    }

    @When("a step passes")
    public void aStepPasses() {
    }

    @After
    public void afterWithAttachment(Scenario scenario) throws Exception {
        attachImage(scenario);
    }

    private static void attachImage(Scenario scenario) throws IOException {
        byte[] bytes = read("/io/cucumber/compatibilitykit/features/hooks-attachment/cucumber.svg");
        scenario.attach(bytes, "image/svg+xml", null);
    }

}
