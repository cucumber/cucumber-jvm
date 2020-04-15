package io.cucumber.compatibility.parametertypes;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParameterTypes {

    static class Flight {
        public final String from;
        public final String to;

        public Flight(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }

    @Given("{flight} has been delayed {int} minutes")
    public void lhrCDGHasBeenDelayedMinutes(Flight flight, int delay) {
        assertEquals("LHR", flight.from);
        assertEquals("CDG", flight.to);
        assertEquals(45, delay);
    }

    @ParameterType("([A-Z]{3})-([A-Z]{3})")
    public Flight flight(String from, String to) {
        return new Flight(from, to);
    }

}
