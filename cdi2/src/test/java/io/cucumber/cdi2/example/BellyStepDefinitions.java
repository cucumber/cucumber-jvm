package io.cucumber.cdi2.example;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BellyStepDefinitions {

    @Inject
    private Belly belly;

    @Given("I have {int} cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
    }

    @Given("I eat {int} more cukes")
    public void addCukes(int n) {
        belly.setCukes(belly.getCukes() + n);
    }

    @Then("there are {int} cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(n, belly.getCukes());
    }

}
