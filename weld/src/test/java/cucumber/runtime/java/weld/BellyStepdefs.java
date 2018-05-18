package cucumber.runtime.java.weld;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Singleton
public class BellyStepdefs {

    @Inject
    private Belly belly;

    private boolean inTheBelly = false;

    @Given("I have {int} cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
        inTheBelly = true;
    }

    @Then("there are {int} cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(n, belly.getCukes());
        assertTrue(inTheBelly);
    }
}
