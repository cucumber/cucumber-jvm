package io.cucumber.compatibility.attachments;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class Attachments {

    Scenario scenario;

    @Before
    public void before(Scenario scenario){
        this.scenario = scenario;
    }

    @Given("the string {word} is attached as {word}")
    public void theStringIsAttachedAs(String text, String contentType){
        scenario.write(text);
    }

    @When("a stream with {int} bytes are attached as {string}")
    public void aStreamWithBytesAreAttachedAs(int n, String mediaType) {
        byte[] bytes = new byte[n];
        for (byte i = 0; i < n; i++) {
            bytes[i] = i;
        }
        scenario.embed(bytes, mediaType, null);
    }

    @When("an array with {int} bytes are attached as {string}")
    public void anArrayWithBytesAreAttachedAs(int n, String mediaType) {
        byte[] bytes = new byte[n];
        for (byte i = 0; i < n; i++) {
            bytes[i] = i;
        }
        scenario.embed(bytes, mediaType, null);
    }


}
