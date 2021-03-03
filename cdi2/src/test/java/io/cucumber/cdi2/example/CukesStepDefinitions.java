package io.cucumber.cdi2.example;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CukesStepDefinitions {

    static int cukes;

    @Given("I have {int} cukes")
    public void haveCukes(int n) {
        cukes = n;
    }

    @Given("I add {int} more cukes")
    public void addCukes(int n) {
        cukes += n;
    }

    @Then("there are {int} cukes")
    public void checkCukes(int n) {
        assertEquals(n, cukes);
    }

}
