package cucumber.runtime.java.test;

import cucumber.api.java8.En;

import static cucumber.api.java8.StepdefBody.A2;
import static org.junit.Assert.assertEquals;

public class LambdaStepdefs implements En {
    @Override
    public void defineGlue() {
        Given("I have (\\d+) cukes in my (.*)", (A2<Integer, String>) (Integer cukes, String what) -> {
            assertEquals(42, cukes.intValue());
            assertEquals("belly", what);
        });
    }
}
