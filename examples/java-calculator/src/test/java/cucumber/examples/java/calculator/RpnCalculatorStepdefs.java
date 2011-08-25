package cucumber.examples.java.calculator;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import static junit.framework.Assert.assertEquals;

public class RpnCalculatorStepdefs {
    private RpnCalculator calc = new RpnCalculator();

    @When("^I add (\\d+) and (\\d+)$")
    public void adding(int arg1, int arg2) {
        calc.push(arg1);
        calc.push(arg2);
        calc.push("+");
    }

    @Then("^the result is (\\d+)$")
    public void the_result_is(double expected) {
        assertEquals(expected, calc.value());
    }

    @Before
    public void before() {

    }

    @After
    public void after() {

    }
}
