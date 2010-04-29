package simple;

import cuke4duke.CellConverter;
import cuke4duke.Table;
import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.annotation.Pending;

import java.util.*;

import static org.junit.Assert.assertEquals;

// TODO: This is just testing a Map. We should have some own code to test!!
public class StuffSteps {
    private final Map<String, Integer> cukes;

    public StuffSteps() {
        cukes = new HashMap<String, Integer>();
    }

    @Pending("Let's procrastinate")
    @Given("a pending step")
    public void intentionallyPending() {
        throw new RuntimeException("We shouldn't get here because we are @Pending");
    }

    @Given("a step definition that is never used")
    public void neverUsed() {
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

        List<Map<String, String>> hashes = new ArrayList<Map<String, String>>();
        hashes.add(hash("a", "1", "b", "2"));
        hashes.add(hash("a", "1", "b", "2"));

        Map<String, Boolean> options = new HashMap<String, Boolean>();
        options.put("surplus_row", false);
        table.diffHashes(hashes, options);
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

    public void thisIsNotAStep() {
    }

    private Map<String, String> hash(String... values) {
        Map<String, String> hash = new HashMap<String, String>();

        for (int i = 0; i < values.length; i += 2) {
            hash.put(values[i], values[i + 1]);
        }

        return hash;
    }

    @Given("a table that we convert:")
    public void convertTable(Table t) {
        t.mapColumn("b", new CellConverter() {
            public String convertCell(String cellValue) {
                return "converted_" + cellValue;
            }
        });
        t.mapHeaders(new HashMap<Object, String>() {{
            put("a", "A");
        }});

        List<Map<String, String>> hashes = new ArrayList<Map<String, String>>();
        hashes.add(hash("A", "eenie", "b", "converted_meenie"));
        hashes.add(hash("A", "miney", "b", "converted_moe"));
        assertEquals(hashes, t.hashes());
    }
}
