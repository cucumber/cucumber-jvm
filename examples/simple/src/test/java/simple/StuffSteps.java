package simple;

import cuke4duke.Given;
import cuke4duke.Table;
import cuke4duke.Then;
import cuke4duke.When;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

// TODO: This is just testing a Map. We should have some own code to test!!
public class StuffSteps {
    private final Map<String,Integer> cukes = new HashMap<String,Integer>();

    @Given("I have (\\d+) (.*) cukes")
    public void iHaveNCukes(int n, String color) {
        this.cukes.put(color, n);
    }

    @When("I add a table")
    public void iAddATable(Table table) {
        table.diffLists(Arrays.asList(
           Arrays.asList("a", "b"),
           Arrays.asList("1", "2")
        ));
        List<Map<String, String>> hashes = new ArrayList(table.hashes());
        Map<String, String> newRow = hashes.get(0);
        hashes.add(newRow);
        table.diffHashes(hashes, new HashMap(){{
            put("surplus_row", false);
        }});
    }

    @Then("I should have (\\d+) (.*) cukes")
    public void iShouldHaveNCukes(int n, String color) {
        int number = cukes.get(color) != null ? cukes.get(color) : 0;
		assertEquals(n, number);
    }

    public void thisIsNotAStep() {}
}
