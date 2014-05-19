package cucumber.runtime.java.spring.commonglue;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;

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
