package gradle.cucumber;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

public class BasicStepdefs {

    @Given("^I use Cucumber Main class to run tests$")
    public void I_use_Cucumber_Main_class_to_run_tests() throws Throwable {
        //do nothing
    }

    @When("^I run failing test$")
    public void I_run_failing_test() throws Throwable {
        new Production().doWork();
    }
}
