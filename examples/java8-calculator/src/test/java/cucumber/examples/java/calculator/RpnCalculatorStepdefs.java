package cucumber.examples.java.calculator;

import cucumber.api.Scenario;
import io.cucumber.datatable.DataTable;
import cucumber.api.java8.En;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RpnCalculatorStepdefs implements En {
    private RpnCalculator calc;

    public RpnCalculatorStepdefs() {
        Given("a calculator I just turned on", () -> {
            calc = new RpnCalculator();
        });

        When("I add {int} and {int}", (Integer arg1, Integer arg2) -> {
            calc.push(arg1);
            calc.push(arg2);
            calc.push("+");
        });


        Given("^I press (.+)$", (String what) -> calc.push(what));

        Then("the result is {double}", (Integer expected) -> assertEquals(expected, calc.value()));

        Then("the result is {int}", (Integer expected) -> assertEquals(expected.doubleValue(), calc.value()));


        Before(new String[]{"not @foo"}, (Scenario scenario) -> {
            scenario.write("Runs before scenarios *not* tagged with @foo");
        });

        After((Scenario scenario) -> {
            // result.write("HELLLLOO");
        });


        Given("the previous entries:", (DataTable dataTable) -> {
            List<Entry> entries = dataTable.asList(Entry.class);
            for (Entry entry : entries) {
                calc.push(entry.first);
                calc.push(entry.second);
                calc.push(entry.operation);
            }
        });

    }

    public static final class Entry {
        private Integer first;
        private Integer second;
        private String operation;

        public Integer getFirst() {
            return first;
        }

        public void setFirst(Integer first) {
            this.first = first;
        }

        public Integer getSecond() {
            return second;
        }

        public void setSecond(Integer second) {
            this.second = second;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }
    }
}
