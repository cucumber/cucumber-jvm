package io.cucumber.compatibility.examplestable;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ExamplesTable {

    @When("I eat {int} cucumbers")
    public void iEatEatCucumbers(int cucumbers) {
    }

    @Then("I should have {int} cucumbers")
    public void iShouldHaveLeftCucumbers(int cucumbers) {
    }

    @Given("there are {int} cucumbers")
    public void thereAreStartCucumbers(int cucumbers) {

    }
}
