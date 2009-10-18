package simple;

import static junit.framework.Assert.assertEquals;
import cuke4duke.Given;
import cuke4duke.Then;
import cuke4duke.When;
public class TransformSteps {
    
    @Given("^I pass '(.*)' to a method with int as parameter$")
    public void transformedToA(int value) {
        assertEquals(10, value);
    }
    
    @Given("^I pass '(.*)' to a method with Car as parameter$")
    public void transformedToA(Car value) {
        assertEquals(10, value);
    }
    
    @When("^something happens$")
    public void somethingHappens() {
    }
    @Then("^all is good$")
    public void allIsGood() {
    }
    
    private class Car {
        
    }
}
