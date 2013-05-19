package cucumber.android.test;

import android.test.ActivityInstrumentationTestCase2;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class CucumberActivitySteps extends ActivityInstrumentationTestCase2<CucumberActivity> {
    public CucumberActivitySteps() {
        super(CucumberActivity.class);
    }

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Given("^I have a test$")
    public void I_have_a_test() {
    }

    @When("^I test$")
    public void I_test() {
    }

    @Then("^I succeed$")
    public void I_succeed() {
    }
}
