package cucumber.runtime.java.spring;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.Assert.assertEquals;

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
