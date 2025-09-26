package io.cucumber.compatibility.ambiguous;

import io.cucumber.java.en.Given;

public class Ambiguous {
    @Given("^is a (.*?) with (.*?)$")
    public void first_ambiguous_step(String a, String b) {

    }

    @Given("^is a step with (.*?)$")
    public void second_ambiguous_step(String a) {

    }

}
