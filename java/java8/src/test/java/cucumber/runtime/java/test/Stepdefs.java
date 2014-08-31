package cucumber.runtime.java.test;

import cucumber.api.java8.En;

public class Stepdefs implements En {
    @Override
    public void defineGlue() {
        Given("I have (\\d+) cukes in my belly", (n) -> System.out.println("Cukes: " + n));
    }
}
