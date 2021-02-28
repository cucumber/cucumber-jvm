package io.cucumber.compatibility.minimal;

import io.cucumber.java.en.Given;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Minimal {

    @Given("I have {int} cukes in my belly")
    public void I_have_int_cukes_in_my_belly(int cukeCount) {
        assertEquals(42, cukeCount);
    }

}
