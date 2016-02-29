package cucumber.runtime.java.spring.contextconfigannotations;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@ContextConfiguration(classes=AnnotationBasedSpringConfig.class)
public class ConfigStep {

	@Autowired
	private ToInject toInject;
	
	@Autowired
	private AnotherComponent anotherComponent;
	
	@Given("^a new step$")
	public void givenANewStep(){
		
	}
	
	@When("^something happens$")
	public void whenSomethingHappens(){
		
	}
	
	@Then("^injected beans shouldn't be nulls$")
	public void something(){
		Assert.assertNotNull(toInject);
	}
}
