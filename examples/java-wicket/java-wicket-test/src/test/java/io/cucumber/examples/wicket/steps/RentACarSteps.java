package io.cucumber.examples.wicket.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RentACarSteps {
    private RentACarSupport rentACarSupport = new RentACarSupport();

    @Given("there are {int} cars available for rental")
    public void there_are_cars_available_for_rental(int availableCars) throws Throwable {
        rentACarSupport.createCars(availableCars);
    }

    @When("I rent one")
    public void rent_one_car() throws Throwable {
        rentACarSupport.rentACar();
    }

    @Then("there will only be {int} cars available for rental")
    public void there_will_be_less_cars_available_for_rental(int expectedAvailableCars) throws Throwable {
        int actualAvailableCars = rentACarSupport.getAvailableNumberOfCars();
        assertThat(actualAvailableCars, is(expectedAvailableCars));
    }
}
