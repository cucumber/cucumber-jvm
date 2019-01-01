package io.cucumber.openejb;

import io.cucumber.java.api.Before;
import io.cucumber.java.api.en.Given;

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
