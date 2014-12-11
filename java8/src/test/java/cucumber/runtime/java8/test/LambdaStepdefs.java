package cucumber.runtime.java8.test;

import cucumber.api.java8.En;

import static org.junit.Assert.assertEquals;

public class LambdaStepdefs implements En {
    @Override
    public void defineGlue() {
        Given("I have (\\d+) cukes in my (.*)", (Integer cukes, String what) -> {
            assertEquals(42, cukes.intValue());
            assertEquals("belly", what);
        });
    }
}
