package io.cucumber.jakarta.cdi.example;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ApplicationScoped
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
