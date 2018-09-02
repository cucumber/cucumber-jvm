package io.cucumber.picocontainer;

import cucumber.api.PendingException;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StepDefs {

    private final DisposableCucumberBelly belly;

    public StepDefs(DisposableCucumberBelly belly) {
        this.belly = belly;
    }

    DisposableCucumberBelly getBelly() {
        return belly;
    }

    @Before
    public void before() {
    }

    @Before("@gh210")
    public void gh20() {
    }

    @After
    public void after() {
        // We might need to clean up the belly here, if it represented an external resource.
        assert !belly.isDisposed();
    }

    @Given("I have {int} {word} in my belly")
    public void I_have_n_things_in_my_belly(int n, String what) {
        belly.setContents(Collections.nCopies(n, what));
    }

    @Given("I have this in my basket:")
    public void I_have_this_in_my_basket(List<List<String>> stuff) {
    }

    @Given("something pending")
    public void throw_pending() {
        throw new PendingException("This should not fail (seeing this output is ok)");
    }

    @Then("there are {int} cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(belly.getContents(), Collections.nCopies(n, "cukes"));
    }

    @Then("the {word} contains {word}")
    public void containerContainsIngredient(String container, String ingredient) throws InterruptedException {
        assertEquals("glass", container);
    }

    @Then("I add {word}")
    public void addLiquid(String liquid) throws InterruptedException {
        assertEquals("milk", liquid);
    }

    @Then("I should be {word}")
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
