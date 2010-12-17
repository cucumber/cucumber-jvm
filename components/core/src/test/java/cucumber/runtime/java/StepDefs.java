package cucumber.runtime.java;

import cucumber.annotation.I18n;

import static junit.framework.Assert.assertEquals;

public class StepDefs {
    private String cukes;

    @I18n.EN.Given("^I have (\\d+) cukes in my belly")
    public void haveCukes(String n) {
        this.cukes = n;
    }

    @I18n.EN.Then("^there are (\\d+) cukes in my belly")
    public void checkCukes(String n) {
        assertEquals(cukes, n);
    }

    @I18n.EN.Then("^the (.*) contains (.*)")
    public void containerContainsIngredient(String container, String ingredient) throws InterruptedException {
        assertEquals("glass", container);
    }

    @I18n.EN.Then("^I add (.*)")
    public void addLiquid(String liquid) throws InterruptedException {
        assertEquals("milk", liquid);
    }
}
