package cucumber.runtime.java.spring;

import cucumber.annotation.en.Given;

public class UnusedStepdefs {
    public UnusedStepdefs() {
        throw new IllegalStateException();
    }

    @Given("unused")
    public void unused() {
        throw new IllegalStateException();
    }
}
