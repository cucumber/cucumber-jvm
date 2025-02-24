package io.cucumber.compatibility.rules;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Rules {

    @Given("the customer has {int} cents")
    public void theCustomerHasCents(int arg0) {
    }

    @And("there are chocolate bars in stock")
    public void thereAreChocolateBarsInStock() {
    }

    @When("the customer tries to buy a {int} cent chocolate bar")
    public void theCustomerTriesToBuyACentChocolateBar(int arg0) {
    }

    @Then("the sale should not happen")
    public void theSaleShouldNotHappen() {
    }

    @Then("the sale should happen")
    public void theSaleShouldHappen() {
    }

    @And("there are no chocolate bars in stock")
    public void thereAreNoChocolateBarsInStock() {
    }
}
