package cucumber.runtime.java.picocontainer;

import cucumber.DateFormat;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class DatesSteps {
    private Date date;

    @Given("^the ISO date is (.+)$")
    public void the_iso_date_is(@DateFormat("yyyy-MM-dd'T'HH:mm:ss") Date date) {
        this.date = date;
    }

    @Given("^the simple date is (.+)$")
    public void the_simple_date_is(@DateFormat("yyyy/MM/dd") Date date) {
        this.date = date;
    }

    @Given("^the ISO date with timezone is (.+$)")
    public void the_ISO_date_with_timezone_is(@DateFormat("yyyy-MM-dd'T'HH:mm:ss, z") Date date) {
        this.date = date;
    }


    @Then("^the date should be viewed in (.+) as (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+)$")
    public void the_date_should_be_decomposed_as(String timeZone, int year, int month, int day, int hours,
                                                 int minutes, int seconds) {
        Calendar cal;
        if (timeZone.equals("default")) {
            cal = Calendar.getInstance(TimeZone.getDefault());
        } else {
            cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        }
        cal.setLenient(false);
        cal.setTime(date);
        assertEquals(year, cal.get(Calendar.YEAR));
        assertEquals(month, cal.get(Calendar.MONTH) + 1); //calendar month are 0 based
        assertEquals(day, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(hours, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(minutes, cal.get(Calendar.MINUTE));
        assertEquals(seconds, cal.get(Calendar.SECOND));
    }
}
