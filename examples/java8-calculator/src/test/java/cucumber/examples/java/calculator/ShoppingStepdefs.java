package cucumber.examples.java.calculator;

import io.cucumber.datatable.DataTable;
import cucumber.api.java8.En;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs implements En {

    private RpnCalculator calc = new RpnCalculator();

    public ShoppingStepdefs() {


        Given("the following groceries:", (DataTable dataTable) -> {
            List<Grocery> groceries = dataTable.asList(Grocery.class);
            for (Grocery grocery : groceries) {
                calc.push(grocery.price.value);
                calc.push("+");
            }
        });

        When("I pay {}", (Integer amount) -> {
            calc.push(amount);
            calc.push("-");
        });

        Then("my change should be {}", (Integer change) -> {
            assertEquals(-calc.value().intValue(), change.intValue());
        });
    }

    static class Grocery {
        private String name;
        private Price price;

        Grocery(String name, Price price) {
            this.name = name;
            this.price = price;
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
