package simple;

import cuke4duke.StepMother;
import cuke4duke.Steps;
import cucumber.annotation.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;
import org.springframework.beans.factory.annotation.Autowired;

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
