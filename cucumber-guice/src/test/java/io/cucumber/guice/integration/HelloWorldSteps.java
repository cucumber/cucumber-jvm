package io.cucumber.guice.integration;

import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Given;

@ScenarioScoped
final class HelloWorldSteps {

    @Given("I have {int} cukes in my belly")
    void I_have_cukes_in_my_belly(int n) {

    }

}
