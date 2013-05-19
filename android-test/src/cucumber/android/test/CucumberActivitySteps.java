package cucumber.android.test;

import android.test.ActivityInstrumentationTestCase2;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class CucumberActivitySteps extends ActivityInstrumentationTestCase2<CucumberActivity> {
    private int mSteps;

    public CucumberActivitySteps() {
        super(CucumberActivity.class);
    }

    @Before
    public void before() {
        assertEquals(0, mSteps);
    }

    @After
    public void after() {
        assertEquals(3, mSteps);
    }

    @Given("^I have a test$")
    public void I_have_a_test() {
        assertEquals(1, ++mSteps);
    }

    @When("^I test$")
    public void I_test() {
        assertEquals(2, ++mSteps);
    }

    @Then("^I succeed$")
    public void I_succeed() {
        assertEquals(3, ++mSteps);
    }
}
