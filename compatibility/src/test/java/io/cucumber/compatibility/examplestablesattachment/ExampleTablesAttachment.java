package io.cucumber.compatibility.examplestablesattachment;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExampleTablesAttachment {

    Scenario scenario;

    @Before
    public void before(Scenario scenario) {
        this.scenario = scenario;
    }

    @When("a JPEG image is attached")
    public void aJPEGImageIsAttached() throws IOException {
        Path path = Paths.get("src/test/resources/features/attachments/cucumber.jpeg");
        byte[] bytes = Files.readAllBytes(path);
        String fileName = path.getFileName().toString();
        scenario.attach(bytes, "image/jpeg", fileName);
    }

    @When("a PNG image is attached")
    public void aPNGImageIsAttached() throws IOException {
        Path path = Paths.get("src/test/resources/features/attachments/cucumber.png");
        byte[] bytes = Files.readAllBytes(path);
        String fileName = path.getFileName().toString();
        scenario.attach(bytes, "image/png", fileName);
    }

}
