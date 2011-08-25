package cucumber.runtime.java.spring;

import static junit.framework.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;

public class BellyStepdefs {
    private final Belly belly;

    @Autowired
    public BellyStepdefs(final Belly belly) {
        this.belly = belly;
    }

    @Then("^there are (\\d+) cukes in my belly")
    public void checkCukes(final int n) {
        assertEquals(n, belly.getCukes());
    }

    @Given("^I have (\\d+) cukes in my belly")
    public void haveCukes(final int n) {
        belly.setCukes(n);
    }
}
