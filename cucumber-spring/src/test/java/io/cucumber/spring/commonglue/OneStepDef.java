package io.cucumber.spring.commonglue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

public class OneStepDef {

    int cucumbers;

    @Autowired
    private ThirdStepDef thirdStepDef;

    public ThirdStepDef getThirdStepDef() {
        return thirdStepDef;
    }

    @Given("the StepDef injection works")
    public void the_StepDef_injection_works() {
        // blank
    }

    @When("I assign the \"cucumbers\" attribute to {int} in one step def class")
    public void i_assign_the_cucumbers_attribute_to_in_one_step_def_class(int arg1) {
        cucumbers = arg1;
        thirdStepDef.cucumbers = arg1;
    }

}
