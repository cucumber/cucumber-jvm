package cucumber.runtime.java8.test;

import cucumber.api.Scenario;
import cucumber.api.java8.En;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class LambdaStepdefs implements En {
    private static LambdaStepdefs lastInstance;

    public LambdaStepdefs() {
        Before((Scenario scenario) -> {
            assertNotSame(this, lastInstance);
            lastInstance = this;
        });

        Given("I have (\\d+) cukes in my (.*)", (Integer cukes, String what) -> {
            assertEquals(42, cukes.intValue());
            assertEquals("belly", what);
        });
    }
}
