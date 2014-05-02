package cucumber.runtime.java.spring;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:cucumber.xml")
public class OneStepDef {
    int cucumbers;

    @Given("^the StepDef injection works$")
    public void the_StepDef_injection_works() throws Throwable {
        // blank
    }

    @When("^I assign the \"cucumbers\" attribute to (\\d+) in one step def class$")
    public void i_assign_the_cucumbers_attribute_to_in_one_step_def_class(int arg1) throws Throwable {
        cucumbers = arg1;
    }

}
