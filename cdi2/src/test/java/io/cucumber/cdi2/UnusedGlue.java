package io.cucumber.cdi2;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

public class UnusedGlue {

    public UnusedGlue() {
        throw new IllegalStateException();
    }

    @Given("unused")
    public void unused() {
        throw new IllegalStateException();
    }

    @Before("@unused")
    public void unusedHook() {
        throw new IllegalStateException();
    }

}
