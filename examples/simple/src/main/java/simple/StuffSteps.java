package simple;

import java.util.Map;
import java.util.HashMap;
import cucumber.*;
import static org.junit.Assert.assertEquals;

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
        assertEquals((long)n, (long)cukes.get(color));
    }

    public void thisIsNotAStep() {}
}
