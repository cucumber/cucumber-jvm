package simple;

import cuke4duke.Before;
import cuke4duke.Then;
import static org.junit.Assert.assertEquals;

public class HookSteps {
    private String b4;

    @Before("")
    public void setB4(Object scenario) {
        b4 = "b4 was here";
    }
    
    @Then("^b4 should have the value \"([^\"]*)\"$")
    public void thenB4(String b4Value) {
        assertEquals(b4Value, b4);
    }
}
