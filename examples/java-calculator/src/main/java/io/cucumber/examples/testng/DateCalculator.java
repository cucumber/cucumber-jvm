package io.cucumber.examples.testng;

import java.util.Date;

public class DateCalculator {
    private Date now;

    public DateCalculator(Date now) {
        this.now = now;
    }

    public String isDateInThePast(Date date) {
        return (date.before(now)) ? "yes" : "no";
    }
}
