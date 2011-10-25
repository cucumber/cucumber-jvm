package cucumber.runtime.java.picocontainer;

import cucumber.annotation.DateFormat;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DatesSteps {
    private Date date;

    @Given("^the date is (.+)$")
    public void the_date_is(@DateFormat("yyyy/MM/dd") Date date) {
        this.date = date;
    }

    @Then("^the date should be (.+)$")
    public void the_date_should_be(@DateFormat("yyyy/MM/dd") Date date) {
        assertEquals(this.date, date);
    }
}
