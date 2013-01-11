package cucumber.runtime.java.picocontainer.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class PicoConfigurerStepdefs {

    private final GreeterInterface greeter;
    private String greeting;

    public PicoConfigurerStepdefs(GreeterInterface greeter) {
        this.greeter = greeter;
    }

    @Given("^I am greeted$")
    public void I_am_greeted() throws Throwable {
        greeting = greeter.greet();
    }

    @Then("^the greeter configured in my custom PicoConfigurer is used$")
    public void the_greeter_configured_in_my_custom_Pico_Configurer_is_used() throws Throwable {
        assertThat(greeting, is(new GreeterImplementation().greet()));
    }

}
