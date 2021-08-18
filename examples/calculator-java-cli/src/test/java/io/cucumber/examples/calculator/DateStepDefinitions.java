package io.cucumber.examples.calculator;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DateStepDefinitions {

    private String result;
    private DateCalculator calculator;

    @ParameterType("([0-9]{4})-([0-9]{2})-([0-9]{2})")
    public LocalDate iso8601Date(String year, String month, String day) {
        return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
    }

    @Given("today is {iso8601Date}")
    public void today_is(LocalDate date) {
        calculator = new DateCalculator(date);
    }

    @When("I ask if {iso8601Date} is in the past")
    public void I_ask_if_date_is_in_the_past(LocalDate date) {
        result = calculator.isDateInThePast(date);
    }

    @Then("^the result should be (yes|no)$")
    public void the_result_should_be(String expectedResult) {
        assertThat(result, equalTo(expectedResult));
    }

}
