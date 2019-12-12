package io.cucumber.compatibility.attachments;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;

public class Attachments {

    Scenario scenario;

    @Before
    public void before(Scenario scenario){
        this.scenario = scenario;
    }

    @Given("the string {word} is attached as {word}")
    public void test(String text, String contentType){
        scenario.write(text);
    }
}
