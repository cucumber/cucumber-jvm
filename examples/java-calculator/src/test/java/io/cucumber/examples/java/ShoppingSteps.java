package io.cucumber.examples.java;

import io.cucumber.java.DataTableType;
import io.cucumber.java.DocStringType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShoppingSteps {

    private final RpnCalculator calc = new RpnCalculator();

    private List<Grocery> shoppingList;
    private List<Grocery> shopStock;
    private int groceriesPrice;

    @DataTableType
    public Grocery defineGrocery(Map<String, String> entry) {
        return new Grocery(entry.get("name"), ShoppingSteps.Price.fromString(entry.get("price")));
    }

    @ParameterType(name = "amount", value = "\\d+\\.\\d+\\s[a-zA-Z]+")
    public Amount defineAmount(String value) {
        String[] arr = value.split("\\s");
        return new Amount(new BigDecimal(arr[0]), Currency.getInstance(arr[1]));
    }

    @DocStringType(contentType = "shopping_list")
    public List<Grocery> defineShoppingList(String docstring) {
        return Stream.of(docstring.split("\\s")).map(Grocery::new).collect(Collectors.toList());
    }

    @Given("the following groceries:")
    public void the_following_groceries(List<Grocery> groceries) {
        for (Grocery grocery : groceries) {
            calc.push(grocery.price.value);
            calc.push("+");
        }
    }

    @When("I pay {amount}")
    public void i_pay(Amount amount) {
        calc.push(amount.price);
        calc.push("-");
    }

    @Then("my change should be {}")
    public void my_change_should_be_(int change) {
        assertEquals(-calc.value().intValue(), change);
    }

    @Given("the following shopping list:")
    public void the_following_shopping_list(List<Grocery> list) {
        shoppingList = list;
    }

    @Given("the shop has following groceries:")
    public void the_shop_has_following_groceries(List<Grocery> shopStock) {
        this.shopStock = shopStock;

    }

    @When("I count shopping price")
    public void i_count_shopping_price() {
        shoppingList.forEach(grocery -> {
            for (Grocery shopGrocery : shopStock) {
                if (grocery.equals(shopGrocery)) {
                    groceriesPrice += shopGrocery.price.value;
                }
            }
        });
    }

    @Then("price would be {int}")
    public void price_would_be(int totalPrice) {
        assertEquals(groceriesPrice, totalPrice);
    }

    static class Grocery {

        private String name;
        private Price price;

        public Grocery(String name, Price price) {
            this.name = name;
            this.price = price;
        }

        public Grocery(String name) {
            this.name = name;
        }

        public Price getPrice() {
            return price;
        }

        public void setPrice(Price price) {
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Grocery grocery = (Grocery) o;
            return Objects.equals(name, grocery.name);
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

    static final class Amount {

        private final BigDecimal price;
        private final Currency currency;

        public Amount(BigDecimal price, Currency currency) {
            this.price = price;
            this.currency = currency;
        }

    }

}
