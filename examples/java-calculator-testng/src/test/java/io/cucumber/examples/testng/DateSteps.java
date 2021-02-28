package io.cucumber.examples.testng;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.getDateInstance;
import static java.util.Locale.ENGLISH;
import static org.testng.Assert.assertEquals;

public class DateSteps {

    private String result;
    private DateCalculator calculator;

    @Given("today is {iso-date}")
    public void today_is(Date date) {
        calculator = new DateCalculator(date);
    }

    @When("I ask if {date} is in the past")
    public void I_ask_if_date_is_in_the_past(Date date) {
        result = calculator.isDateInThePast(date);
    }

    @Then("^the result should be (.+)$")
    public void the_result_should_be(String expectedResult) {
        assertEquals(expectedResult, result);
    }

    @ParameterType("(?:.*) \\d{1,2}, \\d{4}")
    public Date date(String date) throws ParseException {
        return getDateInstance(MEDIUM, ENGLISH).parse(date);
    }

    @ParameterType(name = "iso-date", value = "\\d{4}-\\d{2}-\\d{2}")
    public Date isoDate(String date) throws ParseException {
        return new SimpleDateFormat("yyyy-mm-dd").parse(date);
    }

}
