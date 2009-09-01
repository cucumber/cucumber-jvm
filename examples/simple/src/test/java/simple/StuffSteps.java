package simple;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cuke4duke.Given;
import cuke4duke.Pending;
import cuke4duke.Table;
import cuke4duke.Then;
import cuke4duke.When;

// TODO: This is just testing a Map. We should have some own code to test!!
public class StuffSteps {
    private final Map<String,Integer> cukes = new HashMap<String,Integer>();

    @Pending("Let's procrastinate")
    @Given("a pending step")
    public void intentionallyPending() {
        throw new RuntimeException("We shouldn't get here because we are @Pending");
    }

    @Given("a failing step that is preceded by a pending")
    public void intentionallyFailing() {
        throw new RuntimeException("We shouldn't get here either because the previous one is pending");
    }

    @Given("I have (\\d+) (.*) cukes")
    public void iHaveNCukes(int n, String color) {
        this.cukes.put(color, n);
    }

    @When("I add a table")
    public void iAddATable(Table table) {
    	List<List<String>> diffList = new ArrayList<List<String>>();
    	diffList.add(Arrays.asList("a", "b"));
    	diffList.add(Arrays.asList("1", "2"));
        table.diffLists(diffList);
        List<Map<String, String>> hashes = new ArrayList<Map<String, String>>(table.hashes());
        Map<String, String> newRow = hashes.get(0);
        hashes.add(newRow);
        Map<String, Boolean> hashesMap = new HashMap<String, Boolean>();
        hashesMap.put("surplus_row", false);
		table.diffHashes(hashes, hashesMap);
    }

    @When("^I add a string$") 
    public void iAddAString(String s) {
        assertEquals("Hello\nWorld", s);
    } 


    @Then("I should have (\\d+) (.*) cukes")
    public void iShouldHaveNCukes(int n, String color) {
        int number = cukes.get(color) != null ? cukes.get(color) : 0;
		assertEquals(n, number);
    }

    public void thisIsNotAStep() {}
}
