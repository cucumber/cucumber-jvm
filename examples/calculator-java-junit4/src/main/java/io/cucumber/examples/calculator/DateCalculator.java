package io.cucumber.examples.calculator;

import java.time.LocalDate;

public class DateCalculator {

    private final LocalDate now;

    public DateCalculator(LocalDate now) {
        this.now = now;
    }

    public String isDateInThePast(LocalDate date) {
        return (date.isBefore(now)) ? "yes" : "no";
    }

}
