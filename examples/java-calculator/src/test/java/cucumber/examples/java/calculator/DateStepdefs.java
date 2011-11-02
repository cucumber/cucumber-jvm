package cucumber.examples.java.calculator;

import java.util.Date;

import cucumber.annotation.DateFormat;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import static org.junit.Assert.*;

public class DateStepdefs {

	private Date now;
	String result;
	private DateCalculator calculator;
	
	@Given("^today is (.+)$")
	
	public void today_is_(@DateFormat("yyyy-MM-dd") Date date) {
		calculator = new DateCalculator(date);
		now = date;
	}

	@Then("^the result should be (\\d+)$")
	public void the_result_should_be_(String expectedResult) {
	    assertEquals(expectedResult,result);
	}

	@When("^I ask if (.+) in in the past$")
	public void I_ask_how_many_days_ago_was(Date date) {
	    result = calculator.isDateInThePast(date);
	}
}
