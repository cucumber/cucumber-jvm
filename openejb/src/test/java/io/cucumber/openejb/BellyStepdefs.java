package io.cucumber.openejb;

import io.cucumber.java.api.en.Given;
import io.cucumber.java.api.en.Then;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class BellyStepdefs {

    @Inject
    private Belly belly;

    @Given("I have {int} cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
    }

    @Then("there are {int} cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(n, belly.getCukes());
    }
}
