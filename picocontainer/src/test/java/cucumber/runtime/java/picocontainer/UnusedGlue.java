package cucumber.runtime.java.picocontainer;

import cucumber.annotation.Before;
import cucumber.annotation.en.Given;

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
