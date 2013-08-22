package cucumber.runtime.java.spring;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;

public class NonSpringGlue {

    @Given("no spring context")
    public void unused() {
        //no test needed
    }

    @Before("@unused")
    public void unusedHook() {
        //No hook code needed
    }
}
