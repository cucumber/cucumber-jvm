package cucumber.examples.java.helloworld.stepdefs;

import static org.fest.assertions.Assertions.*;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import cucumber.examples.java.helloworld.app.Hello;

public class HelloStepdefs {
    private Hello hello;
    private String hi;

    @Given("^I have a hello app with \"([^\"]*)\"$")
    public void I_have_a_hello_app_with(String greeting) {
        hello = new Hello(greeting);
    }

    @When("^I ask it to say hi$")
    public void I_ask_it_to_say_hi() {
        hi = hello.sayHi();
    }

    @Then("^it should answer with \"([^\"]*)\"$")
    public void it_should_answer_with(String expectedHi) {
        assertThat(hi).isEqualTo(expectedHi).as("The greating was not the expected greating of "+expectedHi);
    }
}
