package cucumber.runtime.java.hk2.integration;

import cucumber.api.java.en.Given;
import cucumber.runtime.java.hk2.ScenarioScoped;

@ScenarioScoped
public class HelloWorldSteps {
    @Given("^I have (\\d+) cukes in my belly$")
    public void I_have_cukes_in_my_belly(int n) {
        n++;
    }
}
