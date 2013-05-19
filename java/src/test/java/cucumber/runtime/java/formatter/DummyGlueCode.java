package cucumber.runtime.java.formatter;


import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Just a dummy glue code which helps for testing
 */
public class DummyGlueCode {

    @Given("^The glue code faker is up$")
    public void The_glue_code_faker_is_up() throws Throwable {
    }

    @cucumber.api.java.en.When("^activity is triggered$")
    public void activity_is_tirggered() throws Throwable {
    }

    @cucumber.api.java.en.Then("^forcing test to fail$")
    public void forcing_test_to_fail() throws Throwable {
        fail("Designed to fail");
    }

    @Then("^force to pass$")
    public void force_to_pass() throws Throwable {
    }

    @Then("^forcing the dummy glue code to (\\d)$")
    public void forcing_the_dummy_glue_code_to_booleanCode(int code) throws Throwable {
        assertTrue(code == 1);
    }
}
