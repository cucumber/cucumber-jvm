package cucumber.runtime.java.openejb;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;

import javax.inject.Inject;

import static junit.framework.Assert.assertEquals;

public class BellyStepdefs {

    @Inject
    private Belly belly;

    @Given("^I have (\\d+) cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
        System.out.println("adding cukes:" + n);
    }

    @Then("^there are (\\d+) cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(n, belly.getCukes());
    }
}
