package simple;


import cuke4duke.StepMother;
import cuke4duke.Steps;
import cuke4duke.annotation.I18n.EN.When;

public class CallingSteps extends Steps {
    public CallingSteps(StepMother stepMother) {
        super(stepMother);
    }

    @When("^I call another step$")
    public void iCallAnotherStep() {
        Given("it is magic");
    }


}
