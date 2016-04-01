package cucumber.runtime.java.test;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class FeatureStepDefs {

    private String featureName = "";
    private Collection<String> tags;

    @Before
    public void get_feature_name(Scenario scenario) {
        featureName = scenario.getFeature().getName();
        tags = scenario.getFeature().getSourceTagNames();
    }

    @Given("^I am running a feature")
    public void i_am_running_a_feature() {

    }

    @When("^I try to get the feature name$")
    public void i_try_to_get_the_feature_name() {

    }

    @Then("^The feature name is \"([^\"]*)\"$")
    public void the_feature_name_is(String featureName) {
        assertEquals(this.featureName, featureName);
    }

    @Then("^The feature name is not \"([^\"]*)\"$")
    public void theFeatureNameIsNot(String featureName) throws Throwable {
        assertNotEquals(this.featureName, featureName);
    }

    @When("^I try to get the feature tag$")
    public void iTryToGetTheFeatureTag() throws Throwable {
        
    }

    @Then("^The feature tag is \"([^\"]*)\"$")
    public void theFeatureTagIs(String tagName) throws Throwable {
        assertTrue(tags.contains(tagName));
    }

    @Then("^The feature tag is not \"([^\"]*)\"$")
    public void theFeatureTagIsNot(String tagName) throws Throwable {
        assertFalse(tags.contains(tagName));
    }
}
