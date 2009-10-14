package simple;

import cuke4duke.When;
import cuke4duke.Steps;
import cuke4duke.StepMother;

public class CallingSteps extends Steps {
    public CallingSteps(StepMother stepMother) {
        super(stepMother);
    }

    @When("^I call another step$")
    public void iCallAnotherStep() {
        Given("it is magic");
    }
}
