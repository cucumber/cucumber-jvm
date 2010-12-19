package cucumber.runtime.java.spring;


import cucumber.annotation.EN.Given;
import cucumber.annotation.EN.Then;

import static junit.framework.Assert.assertEquals;

public class StepDefs {
    private String cukes;

    @Given("^I have (\\d+) cukes in my belly")
    public void haveCukes(String n) {
        this.cukes = n;
    }

    @Then("^there are (\\d+) cukes in my belly")
    public void checkCukes(String n) {
        assertEquals(cukes, n);
    }

    @Then("^the (.*) contains (.*)")
    public void containerContainsIngredient(String container, String ingredient) throws InterruptedException {
        assertEquals("glass", container);
    }

    @Then("^I add (.*)")
    public void addLiquid(String liquid) throws InterruptedException {
        assertEquals("milk", liquid);
    }
}
