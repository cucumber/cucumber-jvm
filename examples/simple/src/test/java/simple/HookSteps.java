package simple;

import cuke4duke.After;
import cuke4duke.Before;
import cuke4duke.Then;
import cuke4duke.When;

import static org.junit.Assert.assertEquals;

public class HookSteps {
    private String b4;
    private static String myStatic = "clean";

    @Before("@b4,@whatever")
    public void setB4(Object scenario) {
        b4 = "b4 was here";
    }

    @Then("^b4 should have the value \"([^\"]*)\"$")
    public void thenB4(String b4Value) {
        assertEquals(b4Value, b4);
    }

    @When("^I set static value to \"([^\"]*)\"$")
    public void setStatic(String newValue) {
        myStatic = newValue;
    }

    @Then("^static value should be \"([^\"]*)\"$")
    public void staticShouldBe(String expected) {
        assertEquals(expected, myStatic);
    }

    @After("")
    public void setAfter(Object scenario) {
        myStatic = "clean";
    }
}
