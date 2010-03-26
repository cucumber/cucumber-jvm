package simple;

import cuke4duke.annotation.I18n.EN.*;
import org.springframework.beans.factory.annotation.Autowired;

import cuke4duke.StepMother;
import cuke4duke.Steps;
import cuke4duke.spring.StepDefinitions;

@StepDefinitions
public class CallingSteps extends Steps {
    @Autowired
    public CallingSteps(StepMother stepMother) {
        super(stepMother);
    }
    
    @When("^I call another step$")
    public void iCallAnotherStep() {
        Given("it is magic");
    }
}
