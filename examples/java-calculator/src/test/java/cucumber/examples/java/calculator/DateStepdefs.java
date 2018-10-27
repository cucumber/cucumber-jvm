package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateStepdefs {
    private String result;
    private DateCalculator calculator;

    @Given("today is {}")
    public void today_is(Date date) {
        calculator = new DateCalculator(date);
    }

    @When("I ask if {} is in the past")
    public void I_ask_if_date_is_in_the_past(Date date) {
        result = calculator.isDateInThePast(date);
    }

    @Then("the result should be {}")
    public void the_result_should_be(String expectedResult) {
        assertEquals(expectedResult, result);
    }
}
