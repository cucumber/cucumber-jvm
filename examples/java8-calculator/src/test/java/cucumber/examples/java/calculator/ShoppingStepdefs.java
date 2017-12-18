package cucumber.examples.java.calculator;

import cucumber.api.DataTable;
import cucumber.api.Transformer;
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


    static class Grocery {
        public String name;
        @XStreamConverter(Price.Converter.class)
        public Price price;

        public Grocery() {
            super();
        }
    }

    static class Price {
        public int value;

        public Price(int value) {
            this.value = value;
        }

        public static class Converter extends Transformer<Price> {
            @Override
            public Price transform(String value) {
                return new Price(Integer.parseInt(value));
            }
        }
    }
}
