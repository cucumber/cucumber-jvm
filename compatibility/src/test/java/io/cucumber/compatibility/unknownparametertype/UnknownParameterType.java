package io.cucumber.compatibility.unknownparametertype;

import io.cucumber.java.en.Given;

public class UnknownParameterType {

    @Given("{airport} is closed because of a strike")
    public void test(String airport) throws Exception {
        throw new Exception("Should not be called because airport type not defined");
    }

}
