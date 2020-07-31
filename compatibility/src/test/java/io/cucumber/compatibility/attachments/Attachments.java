package io.cucumber.compatibility.attachments;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Attachments {

    Scenario scenario;

    @Before
    public void before(Scenario scenario) {
        this.scenario = scenario;
    }

    @When("the string {string} is attached as {string}")
    public void theStringIsAttachedAs(String text, String contentType) {
        scenario.attach(text, contentType, null);
    }

    @When("the string {string} is logged")
    public void theStringIsLogged(String text) {
        scenario.log(text);
    }

    @When("text with ANSI escapes is logged")
    public void theTextWithANSIEscapesIsLogged() {
        scenario.log(
            "This displays a \u001b[31mr\u001b[0m\u001b[91ma\u001b[0m\u001b[33mi\u001b[0m\u001b[32mn\u001b[0m\u001b[34mb\u001b[0m\u001b[95mo\u001b[0m\u001b[35mw\u001b[0m");
    }

    @When("the following string is attached as {string}:")
    public void theFollowingStringIsAttachedAs(String mediaType, String text) {
        scenario.attach(text, mediaType, null);
    }

    @When("an array with {int} bytes is attached as {string}")
    public void anArrayWithBytesAreAttachedAs(int n, String mediaType) {
        byte[] bytes = new byte[n];
        for (byte i = 0; i < n; i++) {
            bytes[i] = i;
        }
        scenario.attach(bytes, mediaType, null);
    }

    @When("a stream with {int} bytes are attached as {string}")
    public void aStreamWithBytesAreAttachedAs(int n, String mediaType) {
        byte[] bytes = new byte[n];
        for (byte i = 0; i < n; i++) {
            bytes[i] = i;
        }
        scenario.attach(bytes, mediaType, null);
    }

    @When("a JPEG image is attached")
    public void aJPEGImageIsAttached() throws IOException {
        Path path = Paths.get("src/test/resources/features/attachments/cucumber-growing-on-vine.jpg");
        byte[] bytes = Files.readAllBytes(path);
        String fileName = path.getFileName().toString();
        scenario.attach(bytes, "image/jpg", fileName);
    }

}
