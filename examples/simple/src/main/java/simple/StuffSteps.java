package simple;

import cucumber.Given;
import cucumber.Table;
import cucumber.Then;
import cucumber.When;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

// TODO: This is just testing a Map. We should have some own code to test!!
public class StuffSteps {
    private final Map<String,Integer> cukes = new HashMap<String,Integer>();

    @Given("I have (\\d+) (.*) cukes")
    public void iHaveNCukes(int n, String color) {
        this.cukes.put(color, n);
    }

    @When("I add a table")
    public void iAddATable(Table table) {
        Map<String,String> hash = table.hashes().get(0);
        assertEquals("1", hash.get("a"));
        assertEquals("2", hash.get("b"));
    }

    @Then("I should have (\\d+) (.*) cukes")
    public void iShouldHaveNCukes(int n, String color) {
        assertEquals(n, (int)cukes.get(color));
    }

    public void thisIsNotAStep() {}
}
