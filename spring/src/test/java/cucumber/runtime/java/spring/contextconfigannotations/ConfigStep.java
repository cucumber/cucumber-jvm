package cucumber.runtime.java.spring.contextconfigannotations;

import org.springframework.test.context.ContextConfiguration;

import cucumber.api.java.en.Given;

@ContextConfiguration(classes=AnnotationBasedSpringConfig.class)
public class ConfigStep {

	@Given("kjhsdkjsdhfkjh")
	public void givenNothing(){
		
	}
}
