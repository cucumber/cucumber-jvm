package cucumber.runtime.java.test;

import cucumber.api.java.en.Given;

import java.util.List;

public class CPH {
    @Given("^I consumed the following last night:$")
    public void i_had_the_following_last_night(List<Consumption> consumptions) throws Throwable {
        System.out.println("arg1 = " + consumptions);
    }

    public static class Consumption {
        public String drink;
        public int when;
    }
}
