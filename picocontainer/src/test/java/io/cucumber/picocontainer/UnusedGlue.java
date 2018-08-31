package io.cucumber.picocontainer;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;

public class UnusedGlue {

    public UnusedGlue() {
        throw new UnsupportedOperationException();
    }

    @Given("this is not used")
    public void hello() {
        throw new UnsupportedOperationException();
    }

    @Before("@unused")
    public void unusedHook() {
        throw new IllegalStateException();
    }
}
