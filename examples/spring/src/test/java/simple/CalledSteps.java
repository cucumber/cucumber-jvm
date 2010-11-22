package simple;

import cucumber.annotation.annotation.I18n.EN.Given;
import cucumber.annotation.annotation.I18n.EN.Then;
import cuke4duke.spring.StepDefinitions;

import static org.junit.Assert.assertTrue;

@StepDefinitions
public class CalledSteps {
    private boolean magic = false;

    @Given("^it is magic$")
    public void itIsMagic() {
        this.magic = true;
    }

    @Then("^magic should happen$")
    public void magicShouldHappen() {
        assertTrue(magic);
    }
}
