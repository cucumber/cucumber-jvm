package simple;

import cuke4duke.*;
import cuke4duke.annotation.English.*;

import static org.junit.Assert.fail;

public class HalfManualSteps extends Steps {
    public HalfManualSteps(StepMother stepMother) {
        super(stepMother);
    }

    @When("^I ask for input$")
    public void askForInput() {
        try {
            String answer = ask("Here is a question for you. Wait 5 secs to make it pass. Type a string and hit enter to fail.", 5);
            fail("You actually typed something!");
        } catch(Exception expected) {
        }
    }

    @Then("^it should time out$")
    public void shouldTimeOut() {
    }
}
