package io.cucumber.examples.testng;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class ShoppingStepDefinitions {

    private final RpnCalculator calc = new RpnCalculator();

    @Given("the following groceries:")
    public void the_following_groceries(List<Grocery> groceries) {
        for (Grocery grocery : groceries) {
            calc.push(grocery.price.value);
            calc.push("+");
        }
    }

    @When("I pay {}")
    public void i_pay(int amount) {
        calc.push(amount);
        calc.push("-");
    }

    @Then("my change should be {}")
    public void my_change_should_be_(int change) {
        assertEquals(-calc.value().intValue(), change);
    }

    @DataTableType
    public Grocery grocery(Map<String, String> entry) {
        return new Grocery(
            entry.get("name"),
            Price.fromString(entry.get("price")));
    }

    static class Grocery {

        private final String name;
        private final Price price;

        Grocery(String name, Price price) {
            this.name = name;
            this.price = price;
        }

    }

    static final class Price {

        private final int value;

        Price(int value) {
            this.value = value;
        }

        static Price fromString(String value) {
            return new Price(Integer.parseInt(value));
        }

    }

}
