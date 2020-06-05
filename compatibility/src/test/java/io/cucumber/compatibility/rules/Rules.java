package io.cucumber.compatibility.rules;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Rules {

    @Given("there are {int} {float} coins inside")
    public void thereAreCoinsInside(int arg0, float arg2) {
    }

    @Given("there are no chocolates inside")
    public void thereAreNoChocolatesInside() {
    }

    @And("there are {int} chocolates inside")
    public void thereAreChocolatesInside(int arg0) {
    }

    @When("the customer tries to buy a {float} chocolate with a {float} coin")
    public void theCustomerTriesToBuyAChocolateWithACoin(float arg0, float arg1) {
    }

    @Then("the sale should not happen")
    public void theSaleShouldNotHappen() {
    }

    @Then("the customer's change should be {int} {float} coin(s)")
    public void theCustomerSChangeShouldBeCoin(int arg0, float arg1) {
    }

}
