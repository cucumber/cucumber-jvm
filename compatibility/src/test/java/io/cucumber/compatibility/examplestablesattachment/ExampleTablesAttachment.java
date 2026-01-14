package io.cucumber.compatibility.examplestablesattachment;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.When;

import java.io.IOException;

import static io.cucumber.compatibility.Resources.read;

public final class ExampleTablesAttachment {

    Scenario scenario;

    @Before
    public void before(Scenario scenario) {
        this.scenario = scenario;
    }

    @When("a JPEG image is attached")
    public void aJPEGImageIsAttached() throws IOException {
        byte[] bytes = read("/io/cucumber/compatibilitykit/features/examples-tables-attachment/cucumber.jpeg");
        scenario.attach(bytes, "image/jpeg", "cucumber.jpeg");
    }

    @When("a PNG image is attached")
    public void aPNGImageIsAttached() throws IOException {
        byte[] bytes = read("/io/cucumber/compatibilitykit/features/examples-tables-attachment/cucumber.png");
        scenario.attach(bytes, "image/png", "cucumber.jpeg");
    }

}
