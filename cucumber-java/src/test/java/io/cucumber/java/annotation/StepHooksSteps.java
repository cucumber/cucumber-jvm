package io.cucumber.java.annotation;

import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import io.cucumber.java.Step;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class StepHooksSteps {

    private final List<String> beforeStepTexts = new ArrayList<>();
    private final List<String> beforeStepKeywords = new ArrayList<>();
    private final List<String> afterStepTexts = new ArrayList<>();
    private final List<String> afterStepKeywords = new ArrayList<>();

    @Before
    public void reset() {
        beforeStepTexts.clear();
        beforeStepKeywords.clear();
        afterStepTexts.clear();
        afterStepKeywords.clear();
    }

    @BeforeStep
    public void beforeStep(Scenario scenario, Step step) {
        assertNotNull(step, "Step should not be null in @BeforeStep");
        beforeStepTexts.add(step.getText());
        beforeStepKeywords.add(step.getKeyword());
    }

    @AfterStep
    public void afterStep(Scenario scenario, Step step) {
        assertNotNull(step, "Step should not be null in @AfterStep");
        afterStepTexts.add(step.getText());
        afterStepKeywords.add(step.getKeyword());
    }

    @Given("I have a step to execute")
    public void i_have_a_step_to_execute() {
        // Step execution - hooks will capture the step info
    }

    @When("I execute another step")
    public void i_execute_another_step() {
        // Step execution - hooks will capture the step info
    }

    @Then("the BeforeStep hook captured the correct step info")
    public void the_before_step_hook_captured_the_correct_step_info() {
        // At this point, BeforeStep has run for Given, When, and this Then step
        // So we check the first two entries
        assertTrue(beforeStepTexts.size() >= 2, "BeforeStep should have captured at least 2 steps");
        assertEquals("I have a step to execute", beforeStepTexts.get(0));
        assertEquals("Given ", beforeStepKeywords.get(0));
        assertEquals("I execute another step", beforeStepTexts.get(1));
        assertEquals("When ", beforeStepKeywords.get(1));
    }

    @Then("the AfterStep hook captured the correct step info")
    public void the_after_step_hook_captured_the_correct_step_info() {
        // AfterStep runs after each step, so by this Then step,
        // it has already captured Given and When steps
        assertTrue(afterStepTexts.size() >= 2, "AfterStep should have captured at least 2 steps");
        assertEquals("I have a step to execute", afterStepTexts.get(0));
        assertEquals("Given ", afterStepKeywords.get(0));
        assertEquals("I execute another step", afterStepTexts.get(1));
        assertEquals("When ", afterStepKeywords.get(1));
    }

}
