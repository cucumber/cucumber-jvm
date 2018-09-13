package io.cucumber.java.annotation;

import io.cucumber.core.api.Scenario;
import io.cucumber.java.api.Before;
import io.cucumber.java.api.annotation.en.Given;
import io.cucumber.java.api.annotation.en.Then;
import io.cucumber.java.api.annotation.en.When;

import static org.junit.Assert.assertEquals;

public class ScenarioStepDefs {

    private String scenarioName = "";

    @Before
    public void get_scenario_name(Scenario scenario) {
       scenarioName = scenario.getName();
    }

    @Given("I am running a scenario")
    public void i_am_running_a_scenario() {

    }

    @When("I try to get the scenario name")
    public void i_try_to_get_the_scenario_name() {

    }

    @Then("The scenario name is {string}")
    public void the_scenario_name_is(String scenarioName) {
        assertEquals(this.scenarioName, scenarioName);
    }
}
