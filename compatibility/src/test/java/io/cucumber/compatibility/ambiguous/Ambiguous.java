package io.cucumber.compatibility.ambiguous;

import io.cucumber.java.en.Given;

public class Ambiguous {

    @Given("^a step with (.*)$")
    public void second_ambiguous_step(String a) {

    }

    @Given("^a (.*?) with (.*?)$")
    public void first_ambiguous_step(String a, String b) {

    }

}
