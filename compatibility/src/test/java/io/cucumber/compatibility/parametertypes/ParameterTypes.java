package io.cucumber.compatibility.parametertypes;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParameterTypes {

    @Given("{flight} has been delayed")
    public void lhrCDGHasBeenDelayedMinutes(Flight flight) {
        assertEquals("LHR", flight.from);
        assertEquals("CDG", flight.to);
    }

    @ParameterType(value = "([A-Z]{3})-([A-Z]{3})", useForSnippets = true)
    public Flight flight(String from, String to) {
        return new Flight(from, to);
    }

    public static class Flight {

        public final String from;
        public final String to;

        public Flight(String from, String to) {
            this.from = from;
            this.to = to;
        }

    }

}
