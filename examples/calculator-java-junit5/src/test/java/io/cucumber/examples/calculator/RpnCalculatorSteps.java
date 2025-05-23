package io.cucumber.examples.calculator;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RpnCalculatorSteps {

    private RpnCalculator calc;

    @BeforeAll
    public static void beforeAll() {
        // Runs before all scenarios
    }

    @AfterAll
    public static void afterAll() {
        // Runs after all scenarios
    }

    @AfterAll
    public static void afterAll2() {
        // Runs after all scenarios
    }

    @Before("not @foo")
    public void before(Scenario scenario) {
        scenario.log("Runs before each scenarios *not* tagged with @foo");
    }

    @After
    public void after(Scenario scenario) {
        scenario.log("Runs after each scenarios");
    }

    @After
    public void after2(Scenario scenario) {
        scenario.log("Runs after each scenarios");
    }

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

    @Given("^I press (.+)$")
    public void I_press(String what) {
        calc.push(what);
    }

    @Then("the result is {int}")
    public void the_result_is(double expected) {
        assertThat(calc.value()).isEqualTo(expected);
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
