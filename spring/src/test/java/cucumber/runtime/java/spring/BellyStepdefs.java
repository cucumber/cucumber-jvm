package cucumber.runtime.java.spring;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static junit.framework.Assert.assertEquals;

@Component
public class BellyStepdefs {
    private final Belly belly;

    @Autowired
    public BellyStepdefs(Belly belly) {
        this.belly = belly;
    }

    private int cukes;

    @Given("^I have (\\d+) cukes in my belly")
    public void haveCukes(int n) {
        belly.setCukes(n);
    }

    @Then("^there are (\\d+) cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(n, belly.getCukes());
    }
}
