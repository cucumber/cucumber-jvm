package cucumber.internal;

import cucumber.Given;
import cucumber.Then;

import java.util.HashMap;
import java.util.Map;

public class CukeSteps {
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
