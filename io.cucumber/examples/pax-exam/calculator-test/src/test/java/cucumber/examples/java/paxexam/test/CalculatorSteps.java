package cucumber.examples.java.paxexam.test;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import cucumber.api.java.Before;
import cucumber.api.java.en.When;
import cucumber.examples.java.paxexam.CalculatorService;

public class CalculatorSteps {

    @Inject
    private CalculatorService calculatorService;

    private int a, b, result;

    @Before
    public void before() {
        a = 0;
        b = 0;
        result = 0;
    }

    @When("^I set a to (\\d+)$")
    public void i_set_a_to(int v) throws Throwable {
        a = v;
    }

    @When("^I set b to (\\d+)$")
    public void i_set_b_to(int v) throws Throwable {
        b = v;
    }

    @When("^I call the calculator service$")
    public void i_call_the_calculator_service() throws Throwable {
        result = calculatorService.add(a, b);
    }

    @When("^the result is (\\d+)$")
    public void the_result_is(int expected) throws Throwable {
        assertEquals(expected, result);
    }
}
