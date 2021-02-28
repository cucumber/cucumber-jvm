package io.cucumber.picocontainer;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.opentest4j.TestAbortedException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepDefinitions {

    private final DisposableCucumberBelly belly;

    public StepDefinitions(DisposableCucumberBelly belly) {
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
        // We might need to clean up the belly here, if it represented an
        // external resource.
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
        throw new TestAbortedException("Skip this!");
        // throw new PendingException("This should not fail (seeing this output
        // is ok)");
    }

    @Then("there are {int} cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(belly.getContents(), Collections.nCopies(n, "cukes"));
    }

    @Then("the {word} contains {word}")
    public void containerContainsIngredient(String container, String ingredient) {
        assertEquals("glass", container);
    }

    @Then("I add {word}")
    public void addLiquid(String liquid) {
        assertEquals("milk", liquid);
    }

    @Then("I should be {word}")
    public void I_should_be(String mood) {
        assertEquals("happy", mood);
    }

    @When("foo")
    public void foo() {
        throw new TestAbortedException("Skip this!");
    }

    @Then("bar concerning a fluffy spiked club")
    public void bar_concerning_a_fluffy_spiked_club() {
        throw new TestAbortedException("Skip this!");
    }

    @Given("something undefined")
    public void something_undefined() {
        // Write code here that turns the phrase above into concrete actions
        // throw new io.cucumber.java.PendingException();
        throw new TestAbortedException("Skip this!");
    }

    @Given("a big basket with cukes")
    public void a_big_basket_with_cukes() {
        // Write code here that turns the phrase above into concrete actions
        // throw new io.cucumber.java.PendingException();
        throw new TestAbortedException("Skip this!");
    }

    @After
    public void letsSeeWhatHappened(Scenario result) {
        if (result.isFailed()) {
            // Maybe take a screenshot!
        }
    }

}
