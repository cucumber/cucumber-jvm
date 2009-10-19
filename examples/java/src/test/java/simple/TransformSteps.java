package simple;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import cuke4duke.Given;
import cuke4duke.StepMother;
import cuke4duke.Steps;
import cuke4duke.Then;
import cuke4duke.Transform;
import cuke4duke.When;

public class TransformSteps extends Steps {
    
    private boolean exceptionThrown = false;
    private User user;

    public TransformSteps(StepMother stepMother) {
        super(stepMother);
    }

    @Given("^I pass '(.*)' to a method with int as parameter$")
    public void transformToA(int value) {
        assertEquals(10, value);
    }

    @Given("^I pass '(.*)' to a method with Car as parameter$")
    public void transformToA(String value) {
        try {
            Given("pass '(.*)' as a Car", value);
        } catch (Exception e) {
            exceptionThrown = true;
        }
    }
    
    @Given("^pass '(.*)' as a Car$")
    public void passACar(Car value) {
    }
    
    @Transform
    public User transformStringToUserWithAge(String age) {
        return new User(Integer.valueOf(age));
    }
    
    @Given("^I pass '(.*)' to a method with User as parameter$")
    public void transformToA(User user) {
        this.user = user;
    }
    
    @When("^something happens$")
    public void somethingHappens() {
    }

    @Then("^all is good$")
    public void allIsGood() {
        assertFalse(exceptionThrown);
    }
    
    @Then("^a User with age '(.*)' is created$")
    public void aUserWithAgeIsCreated(int age) {
        assertTrue(this.user.age == age);
    }
    
    @Then("^an exception is thrown$")
    public void exceptionIsThrown() {
        assertTrue(exceptionThrown);
    }

    private class Car {
    }
    
    private class User {
        public final int age;

        public User(int age){
            this.age = age;
        }
    }
}
