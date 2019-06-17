package io.cucumber.spring.commonglue;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

public class OneStepDef {
    int cucumbers;

    @Autowired
    private ThirdStepDef thirdStepDef;

    public ThirdStepDef getThirdStepDef() {
        return thirdStepDef;
    }

    @Given("the StepDef injection works")
    public void the_StepDef_injection_works() throws Throwable {
        // blank
    }

    @When("I assign the \"cucumbers\" attribute to {int} in one step def class")
    public void i_assign_the_cucumbers_attribute_to_in_one_step_def_class(int arg1) throws Throwable {
        cucumbers = arg1;
        thirdStepDef.cucumbers = arg1;
    }

}
