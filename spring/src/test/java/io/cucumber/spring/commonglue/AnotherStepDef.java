package io.cucumber.spring.commonglue;

import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class AnotherStepDef {

    @Autowired
    OneStepDef oneStepDef;

    @Then("I can read {int} cucumbers from the other step def class")
    public void i_can_read_cucumbers_from_the_other_step_def_class(int arg1) throws Throwable {
        assertEquals(arg1, oneStepDef.cucumbers);
    }

}
