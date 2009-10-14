package simple;

import static org.junit.Assert.assertTrue;
import cuke4duke.Given;
import cuke4duke.Then;
import cuke4duke.spring.StepDefinitions;

@StepDefinitions
public class CalledSteps {
    private boolean magic = false;
    
    @Given("^it is magic$")
    public void itIsMagic() {
        this.magic  = true;
    }
    
    @Then("^magic should happen$")
    public void magicShouldHappen() {
        assertTrue(magic);
    }
}
