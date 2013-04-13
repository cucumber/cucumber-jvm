package cucumber.runtime.java.picocontainer;

import cucumber.api.PendingException;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class StepDefs {
    private int amount;

    @Before
    public void before() {
    }

    @Before("@gh210")
    public void gh20() {
    }

    @Given("^I have (\\d+) (.*) in my belly$")
    public void I_have_n_things_in_my_belly(int amount, String what) {
        this.amount = amount;
    }

    @Given("^I have this in my basket:$")
    public void I_have_this_in_my_basket(List<List<String>> stuff) {
    }

    @Given("something pending")
    public void throw_pending() {
        throw new PendingException("This should not fail (seeing this output is ok)");
    }

    @Then("^there are (\\d+) cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(amount, n);
    }

    @Then("^the (.*) contains (.*)")
    public void containerContainsIngredient(String container, String ingredient) throws InterruptedException {
        assertEquals("glass", container);
    }

    @Then("^I add (.*)")
    public void addLiquid(String liquid) throws InterruptedException {
        assertEquals("milk", liquid);
    }

    @Then("^I should be (.*)$")
    public void I_should_be(String mood) {
        assertEquals("happy", mood);
    }

    @After
    public void letsSeeWhatHappened(Scenario result) {
        if (result.isFailed()) {
            // Maybe take a screenshot!
        }
    }
}
