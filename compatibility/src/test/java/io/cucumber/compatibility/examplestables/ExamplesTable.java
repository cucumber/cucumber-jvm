package io.cucumber.compatibility.examplestables;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExamplesTable {

    private int count;

    @Given("there are {int} cucumbers")
    public void thereAreStartCucumbers(int cucumbers) {
        this.count = cucumbers;
    }

    @When("I eat {int} cucumbers")
    public void iEatEatCucumbers(int eatCount) {
        this.count -= eatCount;
    }

    @Then("I should have {int} cucumbers")
    public void iShouldHaveLeftCucumbers(int expectedCount) {
        assertEquals(expectedCount, this.count);
    }

}
