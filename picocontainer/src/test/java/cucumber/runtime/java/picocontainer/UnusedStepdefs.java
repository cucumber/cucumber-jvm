package cucumber.runtime.java.picocontainer;

import cucumber.annotation.Before;
import cucumber.annotation.en.Given;

public class UnusedStepdefs {

    public UnusedStepdefs() {
        System.out.println("OH MY GOD");
        throw new UnsupportedOperationException();
    }

    @Given("this is not used")
    public void hello() {
        throw new UnsupportedOperationException();
    }
}
