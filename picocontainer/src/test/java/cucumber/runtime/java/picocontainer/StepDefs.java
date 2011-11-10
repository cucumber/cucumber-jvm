package cucumber.runtime.java.picocontainer;

import cucumber.annotation.After;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.runtime.ScenarioResult;

import static junit.framework.Assert.assertEquals;

public class StepDefs {
    private int amount;

    @Given(value="^I have (\\d+) (.*) in my belly$", tags={"@fail-me"})
    public void I_have_n_somthings_in_my_belly(int amount, String what) {
        throw new RuntimeException("BOOM");
    }

    @Given(value="^I have (\\d+) (.*) in my belly$", tags={"~@fail-me"})
    public void I_have_n_things_in_my_belly(int amount, String what) {
        this.amount = amount;
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
    public void letsSeeWhatHappened(ScenarioResult result) {
        if (result.isFailed()) {
            // Maybe take a screenshot!
        }
    }
}
