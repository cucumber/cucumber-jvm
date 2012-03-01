package cucumber.runtime.java.picocontainer;

import cucumber.DateFormat;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class DatesSteps {
    private Date date;

    @Given("^the date is (.+)$")
    public void the_date_is(@DateFormat("yyyy/MM/dd") Date date) {
        this.date = date;
    }

    @Given("^the iso date is (.+)$")
    public void the_iso_date_is(@DateFormat("yyyy-MM-dd'T'HH:mm:ss") Date date) {
        this.date = toMidnight(date);
    }

    @Given("^the iso calendar is (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$")
    public void the_iso_calendar_is(@DateFormat("yyyy-MM-dd'T'HH:mm:ss") Calendar cal) {
        this.date = toMidnight(cal);
    }

    private Date toMidnight(Date date) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(date);
        return toMidnight(cal);
    }

    private Date toMidnight(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Then("^the date should be (.+)$")
    public void the_date_should_be(@DateFormat("MMM dd yyyy") Date date) {
        assertEquals(this.date, date);
    }
}
