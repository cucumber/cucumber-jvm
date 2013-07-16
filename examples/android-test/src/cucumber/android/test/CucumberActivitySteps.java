package cucumber.android.test;

import android.test.ActivityInstrumentationTestCase2;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

// I don't quite get why glue code has to be in a subclass....
public class CucumberActivitySteps extends ActivityInstrumentationTestCase2<CucumberActivity> {
    private int steps;

    public CucumberActivitySteps() {
        super(CucumberActivity.class);
    }

    @Before
    public void before() {
        assertEquals(0, steps);
    }

    @After
    public void after() {
        assertEquals(3, steps);
    }

    @Given("^I have a test$")
    public void I_have_a_test() {
        assertEquals(1, ++steps);
    }

    @When("^I test$")
    public void I_test() {
        assertEquals(2, ++steps);
    }

    @Then("^I succeed$")
    public void I_succeed() {
        assertEquals(3, ++steps);
    }
}
