package io.cucumber.compatibility.attachments;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Attachments {

    Scenario scenario;

    @Before
    public void before(Scenario scenario){
        this.scenario = scenario;
    }

    @When("the string {string} is attached as {string}")
    public void theStringIsAttachedAs(String text, String contentType){
        scenario.write(text);
    }

    @When("the string {string} is logged")
    public void theStringIsLogged(String text){
        scenario.write(text);
    }

    @When("an array with {int} bytes are attached as {string}")
    public void anArrayWithBytesAreAttachedAs(int n, String mediaType) {
        byte[] bytes = new byte[n];
        for (byte i = 0; i < n; i++) {
            bytes[i] = i;
        }
        scenario.embed(bytes, mediaType, null);
    }

    @When("a stream with {int} bytes are attached as {string}")
    public void aStreamWithBytesAreAttachedAs(int n, String mediaType) {
        // TODO: The embed method should take a java.io.InputStream too
        byte[] bytes = new byte[n];
        for (byte i = 0; i < n; i++) {
            bytes[i] = i;
        }
        scenario.embed(bytes, mediaType, null);
    }


    @When("a JPEG image is attached")
    public void aJPEGImageIsAttached() throws IOException {
        Path path = Paths.get("src/test/resources/features/attachments/cucumber-growing-on-vine.jpg");
        byte[] bytes = Files.readAllBytes(path);
        String fileName = path.getFileName().toString();
        scenario.embed(bytes, "image/jpg", fileName);
    }
}
