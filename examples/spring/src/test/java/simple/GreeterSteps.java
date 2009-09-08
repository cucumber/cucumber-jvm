package simple;

import cuke4duke.Given;
import cuke4duke.Then;
import cuke4duke.When;
import cuke4duke.spring.StepDefinitions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.springframework.beans.factory.annotation.Autowired;

@StepDefinitions
public class GreeterSteps {
    @Autowired
    private Greeter greeter;

    private String helloResponse;

    @Given("I have a greeter")
    public void iHaveAGreeter() {
        assertNotNull(greeter);
    }

    @When("I tell the greeter to say hello")
    public void iAskTheWorldForHello() {
        helloResponse = greeter.hello();
    }

    @Then("the response should be \"(.*)\"")
    public void theResponseShouldBe(String response) {
        assertEquals(response, helloResponse);
    }
}
