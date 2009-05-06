package cucumber.internal;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import cucumber.steps.SpringSteps;

public class SpringBasedStepMotherTest {
    
    @Test
	public void shouldInitSpringContext() throws Throwable {
		SpringBasedStepMother mother = new SpringBasedStepMother("steps.xml");
		mother.add(SpringSteps.class);
		mother.newWorld();
		
		List<StepDefinition> stepDefinitions = mother.getStepDefinitions();
		assertEquals(3, stepDefinitions.size());
		
		StepDefinition helloStep = stepDefinitions.get(1);
		helloStep.invokeOnTarget(new Object[]{});
	}
}
