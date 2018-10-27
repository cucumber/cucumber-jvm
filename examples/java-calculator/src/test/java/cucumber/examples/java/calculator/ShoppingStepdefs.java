package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs {
    private RpnCalculator calc = new RpnCalculator();

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

    static class Grocery {
        private String name;
        private Price price;

        public void setPrice(Price price) {
            this.price = price;
        }

        public Price getPrice() {
            return price;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    static final class Price {
        private int value;

        Price(int value) {
            this.value = value;
        }

        static Price fromString(String value) {
            return new Price(Integer.parseInt(value));
        }

    }
}
