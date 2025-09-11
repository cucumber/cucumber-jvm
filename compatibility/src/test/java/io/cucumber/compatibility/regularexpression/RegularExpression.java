package io.cucumber.compatibility.regularexpression;

import io.cucumber.java.en.Given;

public class RegularExpression {
    @Given("^a (.*?)(?: and a (.*?))?(?: and a (.*?))?$")
    public void some_vegetables(String a, String b, String c) {

    }

}
