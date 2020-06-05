package io.cucumber.examples.junit5.calculator;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateStepDefinitions {

    private String result;
    private DateCalculator calculator;

    @Given("^today is ([0-9]{4}-[0-9]{2}-[0-9]{2})$")
    public void today_is(Date date) {
        calculator = new DateCalculator(date);
    }

    @When("^I ask if ([0-9]{4}-[0-9]{2}-[0-9]{2}) is in the past$")
    public void I_ask_if_date_is_in_the_past(Date date) {
        result = calculator.isDateInThePast(date);
    }

    @Then("^the result should be (yes|no)$")
    public void the_result_should_be(String expectedResult) {
        assertEquals(expectedResult, result);
    }

}
