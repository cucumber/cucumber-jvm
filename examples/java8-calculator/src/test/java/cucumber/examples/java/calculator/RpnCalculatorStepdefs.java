package cucumber.examples.java.calculator;

import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java8.En;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RpnCalculatorStepdefs implements En {
    private RpnCalculator calc;

    public RpnCalculatorStepdefs() {
        Given("^a calculator I just turned on$", () -> {
            calc = new RpnCalculator();
        });

        When("^I add (\\d+) and (\\d+)$", (Integer arg1, Integer arg2) -> {
            calc.push(arg1);
            calc.push(arg2);
            calc.push("+");
        });


        Given("^I press (.+)$", (String what) -> calc.push(what));

        Then("^the result is (\\d+)$", (Double expected) -> assertEquals(expected, calc.value()));


        Before(new String[]{"not @foo"}, (Scenario scenario) -> {
            scenario.write("Runs before scenarios *not* tagged with @foo");
        });

        After((Scenario scenario) -> {
            // result.write("HELLLLOO");
        });


        Given("^the previous entries:$", (DataTable dataTable) -> {
            List<Entry> entries = dataTable.asList(Entry.class);
            for (Entry entry : entries) {
                calc.push(entry.first);
                calc.push(entry.second);
                calc.push(entry.operation);
            }
        });

    }

    public class Entry {
        Integer first;
        Integer second;
        String operation;
    }
}
