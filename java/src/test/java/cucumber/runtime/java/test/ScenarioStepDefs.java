package cucumber.runtime.java.test;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.model.CurrentScenario;

import static junit.framework.Assert.assertEquals;

public class ScenarioStepDefs {

    private String scenarioName = "";

    @Given("^I am running a scenario$")
    public void i_am_running_a_scenario() {

    }

    @When("^I try to get the scenario name$")
    public void i_try_to_get_the_scenario_name() {
        scenarioName = CurrentScenario.get().getName();
    }

    @Then("^The scenario name is \"([^\"]*)\"$")
    public void the_scenario_name_is(String scenarioName) {
        assertEquals(this.scenarioName, scenarioName);
    }
}
