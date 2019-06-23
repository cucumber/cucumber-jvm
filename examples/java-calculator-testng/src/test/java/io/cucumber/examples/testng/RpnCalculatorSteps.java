package io.cucumber.examples.testng;

import cucumber.api.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class RpnCalculatorSteps {
    private RpnCalculator calc;

    @Given("a calculator I just turned on")
    public void a_calculator_I_just_turned_on() {
        calc = new RpnCalculator();
    }

    @When("I add {int} and {int}")
    public void adding(int arg1, int arg2) {
        calc.push(arg1);
        calc.push(arg2);
        calc.push("+");
    }

    @Given("I press (.+)")
    public void I_press(String what) {
        calc.push(what);
    }

    @Then("the result is {int}")
    public void the_result_is(double expected) {
        assertEquals(expected, calc.value());
    }

    @Before("not @foo")
    public void before() {
        System.out.println("Runs before scenarios *not* tagged with @foo");
    }

    @After
    public void after(Scenario scenario) {
        // result.write("HELLLLOO");
    }

    @Given("the previous entries:")
    public void thePreviousEntries(List<Entry> entries) {
        for (Entry entry : entries) {
            calc.push(entry.first);
            calc.push(entry.second);
            calc.push(entry.operation);
        }
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
