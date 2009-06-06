package cuke4duke.junit;

import cuke4duke.Given;
import cuke4duke.Then;
import cuke4duke.Steps;

import java.util.HashMap;
import java.util.Map;

@Steps
public class JunitCukeSteps {
    private final Map<String,Integer> cukes = new HashMap<String,Integer>();

    @Given("I have (\\d+) (.*) cukes")
    public void iHaveNCukes(int n, String color) {
        this.cukes.put(color, n);
    }

    @Then("I should have (\\d+) (.*) cukes")
    public void iShouldHaveNCukes(int n, String color) {
        if(n != cukes.get(color)) {
            throw new RuntimeException("Expected " + n + ", got " + cukes.get(color));
        }
    }

    @Given("Longs: (\\d+)")
    public void longs(long n) {
    }

    public void thisIsNotAStep() {}
}