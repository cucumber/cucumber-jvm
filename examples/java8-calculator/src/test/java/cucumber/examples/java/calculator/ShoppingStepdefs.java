package cucumber.examples.java.calculator;

import cucumber.api.Transformer;
import cucumber.api.datatable.DataTable;
import cucumber.api.java8.En;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;


import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs implements En {

    private RpnCalculator calc = new RpnCalculator();

    public ShoppingStepdefs() {


        Given("^the following groceries:$", (DataTable dataTable) -> {
            List<Grocery> groceries = dataTable.asList(Grocery.class);
            for (Grocery grocery : groceries) {
                calc.push(grocery.price.value);
                calc.push("+");
            }
        });

        When("^I pay (\\d+)$", (Integer amount) -> {
            calc.push(amount);
            calc.push("-");
        });

        Then("^my change should be (\\d+)$", (Integer change) -> {
            assertEquals(-calc.value().intValue(), change.intValue());
        });
    }

    public static final class Grocery {
        private String name;
        private Price price;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Price getPrice() {
            return price;
        }

        public void setPrice(Price price) {
            this.price = price;
        }
    }

    public static final class Price {
        private int value;

        public Price(int value) {
            this.value = value;
        }

        public static Price fromString(String value) {
            return new Price(Integer.parseInt(value));
        }

        public int getValue() {
            return value;
        }
    }
}
