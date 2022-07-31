package io.cucumber.java.annotation;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScenarioSteps {

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
