package cucumber.examples.java.calculator;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class RpnCalculatorStepdefs {
	private RpnCalculator calc;

	@Given("^a calculator I just turned on$")
	public void a_calculator_I_just_turned_on() {
		calc = new RpnCalculator();
	}

	@When("^I add (\\d+) and (\\d+)$")
	public void adding(int arg1, int arg2) {
		calc.push(arg1);
		calc.push(arg2);
		calc.push("+");
	}

	@Then("^the result is (\\d+)$")
	public void the_result_is(double expected) {
		assertEquals(expected, calc.value());
	}

	@Before({ "~@foo" })
	public void before() {
		System.out.println("Runs before scenarios *not* tagged with @foo");
	}

	@After
	public void after() {

	}

	/**
	 * Shows you can use the @Given annotation even inside a scenario outline,
	 * having a List<SomeObject> as the argument
	 * @param additions
	 */
	@Given("^the previous additions:$")
	public void thePreviousAdditions(List<PreviousAddition> additions) {
		calc = new RpnCalculator();		
		for (Iterator<PreviousAddition> iterator = additions.iterator(); iterator
				.hasNext();) {
			PreviousAddition operation = iterator.next();
			calc.push(operation.getFirst());
			calc.push(operation.getSecond());
			calc.push("+");
		}
	}

	public class PreviousAddition {
		Integer first;
		Integer second;
		String operation;
		
		public PreviousAddition(Integer first, Integer second, String operation) {
			super();
			this.first = first;
			this.second = second;
			this.operation = operation;
		}

		public Integer getFirst() {
			return first;
		}

		public void setFirst(Integer first) {
			this.first = first;
		}

		public Integer getSecond() {
			return second;
		}

		public void setSecond(Integer second) {
			this.second = second;
		}

		public String getOperation() {
			return operation;
		}

		public void setOperation(String operation) {
			this.operation = operation;
		}
	}
}
