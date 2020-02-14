package io.cucumber.compatibility.rules;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Rules {

    @Then("the customer's change should be {int} {double} coin")
    public void theCustomerSChangeShouldBeCoin(int arg0, int arg1, int arg2) {
    }

    @When("the customer tries to buy a {double} chocolate with a {int} coin")
    public void theCustomerTriesToBuyAChocolateWithACoin(int arg0, int arg1, int arg2) {
    }

    @Then("the sale should not happen")
    public void theSaleShouldNotHappen() {
    }

    @And("there are {int} chocolates inside")
    public void thereAreChocolatesInside(int arg0) {
    }

    @Given("there are {int} {double} coins inside")
    public void thereAreCoinsInside(int arg0, int arg1, int arg2) {
    }

    @Given("there are no chocolates inside")
    public void thereAreNoChocolatesInside() {
    }


}
