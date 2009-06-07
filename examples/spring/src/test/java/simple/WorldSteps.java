package simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;

import simple.World;
import cuke4duke.Given;
import cuke4duke.Steps;
import cuke4duke.Then;
import cuke4duke.When;

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
