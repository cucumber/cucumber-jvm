package cucumber.runtime.java.openejb;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class BellyStepdefs {

    @Inject
    private Belly belly;

    @Given("^I have (\\d+) cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
    }

    @Then("^there are (\\d+) cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(n, belly.getCukes());
    }
}
