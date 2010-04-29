package simple;

import cuke4duke.StepMother;
import cuke4duke.Steps;
import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.annotation.Transform;

import static junit.framework.Assert.*;

public class TransformSteps extends Steps {

    private boolean exceptionThrown = false;
    private User user;
    private boolean yes;

    public TransformSteps(StepMother stepMother) {
        super(stepMother);
    }

    @Transform
    public User transformStringToUserWithAge(String age) {
        return new User(Integer.valueOf(age));
    }

    @Transform
    public boolean overrideBooleanPrimitiveTransform(String boolValue) {
        return boolValue.equals("yes");
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

    @Given("^I pass '(.*)' to a method with User as parameter$")
    public void transformToA(User user) {
        this.user = user;
    }

    @Given("^I pass '(.*)' to a method with boolean as parameter$")
    public void iPassYesToAMethodWithBooleanAsParameter(boolean yes) {
        this.yes = yes;
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

    @Then("^the parameter is true$")
    public void theParameterIsTrue() {
        assertTrue(yes);
    }

    public static class Car {
    }

    public static class User {
        public final int age;

        public User(int age) {
            this.age = age;
        }
    }
}
