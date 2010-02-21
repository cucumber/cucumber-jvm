package simple;

import cuke4duke.After;
import cuke4duke.Before;
import cuke4duke.annotation.English.*;

import static org.junit.Assert.assertEquals;

public class HookSteps extends SuperSteps {
    private String b4WithoutArgs;
    private static String myStatic = "clean";
    private static int beforeHookCount;

    public HookSteps() {
        beforeHookCount = 0;
    }

    @Before("@nothing_tagged_with_this")
    public void cryWolf() {
        throw new RuntimeException("CRY WOLF");
    }

    @Before
    public void setB4WithoutArgs() {
        beforeHookCount++;
        b4WithoutArgs = "b4WithoutArgs was here";
    }

    @Then("^b4 should have the value \"([^\"]*)\"$")
    public void thenB4(String b4Value) {
        assertEquals(b4Value, b4);
    }

    @Then("^b4AndForever should have the value \"([^\"]*)\"$")
    public void thenB4AndForever(String b4AndForeverValue) {
        assertEquals(b4AndForeverValue, b4AndForever);
    }

    @When("^I set static value to \"([^\"]*)\"$")
    public void setStatic(String newValue) {
        myStatic = newValue;
    }

    @Then("^static value should be \"([^\"]*)\"$")
    public void staticShouldBe(String expected) {
        assertEquals(expected, myStatic);
    }

    @After
    public void setAfter(Object scenario) {
        myStatic = "clean";
        assertEquals("b4WithoutArgs was here", b4WithoutArgs);
        assertEquals(1, beforeHookCount);
    }
}
