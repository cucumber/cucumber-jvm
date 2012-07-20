package cucumber.runtime.java.weld;

import cucumber.annotation.Before;
import cucumber.annotation.en.Given;

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
