package simple;

import static org.junit.Assert.assertTrue;

import cuke4duke.annotation.I18n.EN.*;
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
