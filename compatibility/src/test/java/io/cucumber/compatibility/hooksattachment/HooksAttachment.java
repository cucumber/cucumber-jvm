package io.cucumber.compatibility.hooksattachment;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HooksAttachment {

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
        Path path = Paths.get("src/test/resources/features/hooks-attachment/cucumber.svg");
        byte[] bytes = Files.readAllBytes(path);

        scenario.attach(bytes, "image/svg+xml", null);
    }

}
