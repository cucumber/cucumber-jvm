package cucumber.examples.java.calculator;

import static org.junit.Assert.assertEquals;

import cucumber.api.PreviousStepState;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class StatelessCalculatorStepDefs {

	@Given("^I turned on the stateless calculator$")
	public StatelessCalculator statelessCalculatorIsTurnedOn() {
		return new StatelessCalculator();
	}

	@When("^I add (\\d+) to (\\d+)$")
	public int adding(int arg1, int arg2, PreviousStepState previousStepState) {
		StatelessCalculator statelessCalculator = (StatelessCalculator) previousStepState.getResponseFromPreviousStep().get();
		return statelessCalculator.add(arg1, arg2);
	}
	
	@When("^I subtract (\\d+) from (\\d+)$")
	public int subtracting(int arg1, int arg2, PreviousStepState previousStepState) {
		StatelessCalculator statelessCalculator = (StatelessCalculator) previousStepState.getResponseFromPreviousStep().get();
		return statelessCalculator.subtract(arg2, arg1);
	}

	@Then("^The result should be (\\d+)$")
	public void the_result_is(int expected, PreviousStepState previousStepState) {
		assertEquals(expected, previousStepState.getResponseFromPreviousStep().get());
	}

}
