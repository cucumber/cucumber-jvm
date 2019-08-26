package cucumber.examples.java.calculator;

import cucumber.api.Format;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateStepdefs {
    private String result;
    private DateCalculator calculator;

    @Given("^today is (.+)$")
    public void today_is(@Format("yyyy-MM-dd") Date date) {
        calculator = new DateCalculator(date);
    }

    /**
     * We don't need to use @Format here, since the date string in the step
     * conforms to <code>SimpleDateFormat.SHORT</code>. Cucumber has built-in support for
     * <code>SimpleDateFormat.SHORT</code>, <code>SimpleDateFormat.MEDIUM</code>,
     * <code>SimpleDateFormat.LONG</code> and <code>SimpleDateFormat.FULL</code>.
     *
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
     */
    @When("^I ask if (.+) is in the past$")
    public void I_ask_if_date_is_in_the_past(Date date) {
        result = calculator.isDateInThePast(date);
    }

    @Then("^the result should be (.+)$")
    public void the_result_should_be(String expectedResult) {
        assertEquals(expectedResult, result);
    }
}
