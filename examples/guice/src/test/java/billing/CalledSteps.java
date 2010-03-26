package billing;

import cuke4duke.annotation.I18n.EN.*;

import static org.junit.Assert.assertTrue;

public class CalledSteps {
    private boolean magic;

    @Given("^it is (.*)$")
    public void itIs(String what) {
        if(what.equals("magic")) {
            magic = true;
        }
    }

    @Then("^magic should happen$")
    public void magicShouldHappen() {
        assertTrue(magic);
    }
}
