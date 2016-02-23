package cucumber.runtime.java.spring.contextconfigannotations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class BeingInjectedStep {

	@Autowired
	private ToInject1 toInject1;
	
	
	@Given("^a new step$")
	public void givenANewStep(){
		
	}
	
	@When("^something happens$")
	public void whenSomethingHappens(){
		
	}
	
	@Then("^injected beans shouldn't be nulls$")
	public void something(){
		System.out.println("");
	}
}
