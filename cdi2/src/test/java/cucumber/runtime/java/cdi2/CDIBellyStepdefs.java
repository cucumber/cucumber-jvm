package cucumber.runtime.java.cdi2;

import cucumber.api.java.en.Then;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@ApplicationScoped
public class CDIBellyStepdefs {

    @Inject
    private Belly belly;

    @Then("there are {int} cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(n, belly.getCukes());
    }
}
