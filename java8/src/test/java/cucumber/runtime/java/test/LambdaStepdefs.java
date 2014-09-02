package cucumber.runtime.java.test;

import cucumber.api.Scenario;
import cucumber.api.java8.En;

import static org.junit.Assert.assertEquals;

public class LambdaStepdefs implements En {
    @Override
    public void defineGlue() {
        Before((Scenario scenario) -> {
        });

        Given("I have (\\d+) cukes in my (.*)", (Integer cukes, String what) -> {
            assertEquals(42, cukes.intValue());
            assertEquals("belly", what);
        });
    }
}
