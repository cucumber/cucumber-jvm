package cucumber.examples.java.helloworld;

import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.Options;
import com.mdimension.jchronic.utils.Span;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.api.Transform;
import cucumber.api.Transformer;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

public class TimeStepdefs {
    private static Options options = new Options();
    private Calendar laundryDate;

    @Given("^today is \"([^\"]*)\"$")
    public void today_is(Calendar today) throws Throwable {
        options.setNow(today);
    }

    @Given("^I did laundry (.*)")
    public void I_did_laundry_time_ago(@Transform(ChronicConverter.class) Calendar laundryDate) throws Throwable {
        this.laundryDate = laundryDate;
    }

    @Then("^my laundry day must have been \"([^\"]*)\"$")
    public void my_laundry_day_must_have_been(Calendar day) throws Throwable {
        assertEquals(day, laundryDate);
    }

    public static class ChronicConverter extends Transformer<Calendar> {
        @Override
        public Calendar transform(String value) {
            Span span = Chronic.parse(value, options);
            return span.getEndCalendar();
        }
    }
}
