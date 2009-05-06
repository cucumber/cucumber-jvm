package steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import simple.World;
import cucumber.Given;
import cucumber.Steps;
import cucumber.Then;
import cucumber.When;

@Steps
public class WorldSteps {
	@Autowired
	private World world;
	
	private String helloResponse;
	
	@Given("I have a world")
	public void iHaveAWorld() {
		assertNotNull(world);
	}
	
	@When("I tell the world to say hello")
	public void iAskTheWorldForHello() {
		helloResponse = world.hello();
	}
	
	@Then("the response should be (.*)")
	public void theResponseShouldBe(String response) {
		assertEquals(response, helloResponse);
	}
}
