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
    private List<ScenarioMovement> stepDefmovements;

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

    @Before({"~@foo"})
    public void before() {
        System.out.println("Runs before scenarios *not* tagged with @foo");
    }

    @After
    public void after() {

    }
    
	@Given("^the following movements:$")
	public void theFollowingMovements(List<ScenarioMovement> movements) {
		calc = new RpnCalculator();
		stepDefmovements = new ArrayList<RpnCalculatorStepdefs.ScenarioMovement>();
	    for (Iterator<ScenarioMovement> iterator = movements.iterator(); iterator.hasNext();) {
	    	
	    	stepDefmovements.add(iterator.next());	
		}
	}
	
	public class ScenarioMovement {
		String code;
		String from;
		String to;
		
		public ScenarioMovement(String code, String from, String to) {
			super();
			this.code = code;
			this.from = from;
			this.to = to;
		}
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getFrom() {
			return from;
		}
		public void setFrom(String from) {
			this.from = from;
		}
		public String getTo() {
			return to;
		}
		public void setTo(String to) {
			this.to = to;
		}
		
	}
}
