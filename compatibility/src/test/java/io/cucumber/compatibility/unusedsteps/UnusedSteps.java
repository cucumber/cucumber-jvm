package io.cucumber.compatibility.unusedsteps;

import io.cucumber.java.en.Given;

public class UnusedSteps {

    @Given("a step that is used")
    public void a_step_that_is_used() {
        
    }
    
    @Given("a step that is not used")
    public void a_step_that_is_not_used() {
        
    }

}
