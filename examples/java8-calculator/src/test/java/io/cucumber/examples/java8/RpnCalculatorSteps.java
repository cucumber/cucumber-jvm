package io.cucumber.examples.java8;

import cucumber.api.Scenario;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RpnCalculatorSteps implements En {
    private RpnCalculator calc;

    public RpnCalculatorSteps() {
        Given("a calculator I just turned on", () -> {
            calc = new RpnCalculator();
        });

        When("I add {int} and {int}", (Integer arg1, Integer arg2) -> {
            calc.push(arg1);
            calc.push(arg2);
            calc.push("+");
        });


        Given("I press (.+)", (String what) -> calc.push(what));

        Then("the result is {double}", (Integer expected) -> assertEquals(expected, calc.value()));

        Then("the result is {int}", (Integer expected) -> assertEquals(expected.doubleValue(), calc.value()));


        Before("not @foo", (Scenario scenario) -> {
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

    static final class Entry {
        private final Integer first;
        private final Integer second;
        private final String operation;

        Entry(Integer first, Integer second, String operation) {
            this.first = first;
            this.second = second;
            this.operation = operation;
        }
    }
}
