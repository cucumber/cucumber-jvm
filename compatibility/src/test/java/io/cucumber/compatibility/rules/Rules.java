package io.cucumber.compatibility.rules;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Rules {

    @Given("There are {int} {float} coins inside")
    public void thereAreCoinsInside(int arg0, float arg2) {
    }

    @Given("There are no chocolates inside")
    public void thereAreNoChocolatesInside() {
    }

    @And("There are {int} chocolates inside")
    public void thereAreChocolatesInside(int arg0) {
    }

    @When("The customer tries to buy a {float} chocolate with a {float} coin")
    public void theCustomerTriesToBuyAChocolateWithACoin(float arg0, float arg1) {
    }

    @Then("The sale should not happen")
    public void theSaleShouldNotHappen() {
    }

    @Then("The customer's change should be {int} {float} coin(s)")
    public void theCustomerSChangeShouldBeCoin(int arg0, float arg1) {
    }

}
