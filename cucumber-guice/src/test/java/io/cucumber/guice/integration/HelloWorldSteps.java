package io.cucumber.guice.integration;

import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Given;

@ScenarioScoped
public class HelloWorldSteps {

    @Given("I have {int} cukes in my belly")
    public void I_have_cukes_in_my_belly(int n) {

    }

}
