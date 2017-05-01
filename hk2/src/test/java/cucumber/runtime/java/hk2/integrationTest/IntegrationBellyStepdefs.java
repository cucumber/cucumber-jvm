package cucumber.runtime.java.hk2.integrationTest;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.runtime.java.hk2.Belly;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Singleton
public class IntegrationBellyStepdefs {

    private Belly belly;

    @Inject
    IntegrationBellyStepdefs(Belly belly) {
        this.belly = belly;
    }

    private boolean inTheBelly = false;

    @Given("^I have (\\d+) cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
        inTheBelly = true;
    }

    @Then("^there are (\\d+) cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(n, belly.getCukes());
        assertTrue(inTheBelly);
    }
}
