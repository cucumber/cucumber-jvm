package cucumber.runtime.java.test;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static junit.framework.Assert.assertEquals;

public class ScenarioStepDefs {

    private String scenarioName = "";

    @Before
    public void get_scenario_name(Scenario scenario) {
       scenarioName = scenario.getName();
    }

    @Given("^I am running a scenario$")
    public void i_am_running_a_scenario() {

    }

    @When("^I try to get the scenario name$")
    public void i_try_to_get_the_scenario_name() {

    }

    @Then("^The scenario name is \"([^\"]*)\"$")
    public void the_scenario_name_is(String scenarioName) {
        assertEquals(this.scenarioName, scenarioName);
    }
}
