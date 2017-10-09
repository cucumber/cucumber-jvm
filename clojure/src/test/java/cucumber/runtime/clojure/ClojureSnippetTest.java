package cucumber.runtime.clojure;

import cucumber.runtime.Backend;
import cucumber.runtime.io.ResourceLoader;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTable;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ClojureSnippetTest {
    private static final List<Argument> NO_ARGUMENTS = Collections.emptyList();
    private static final List<PickleLocation> NO_LOCATIONS = Collections.emptyList();

    @Test
    public void generatesPlainSnippet() throws Exception {
        PickleStep step = new PickleStep("I have 4 cukes in my \"big\" belly", NO_ARGUMENTS, NO_LOCATIONS);
        String snippet = newBackend().getSnippet(step, "Given", null);
        String expected = "" +
                "(Given #\"^I have (\\d+) cukes in my \\\"([^\\\"]*)\\\" belly$\" [arg1 arg2]\n" +
                "  (comment  Write code here that turns the phrase above into concrete actions  )\n" +
                "  (throw (cucumber.api.PendingException.)))\n";
        assertEquals(expected, snippet);
    }

    @Test
    public void generatesSnippetWithDataTable() throws Exception {
        PickleTable dataTable = new PickleTable(asList(new PickleRow(asList(new PickleCell(null, "col1")))));
        PickleStep step = new PickleStep("I have:", asList((Argument)dataTable), NO_LOCATIONS);
        String snippet = (newBackend()).getSnippet(step, "Given", null);
        String expected = "" +
                "(Given #\"^I have:$\" [arg1]\n" +
                "  (comment  Write code here that turns the phrase above into concrete actions  )\n" +
                "  (throw (cucumber.api.PendingException.)))\n";
        assertEquals(expected, snippet);
    }

    private Backend newBackend() throws Exception {
        return (Backend) Class.forName("cucumber.runtime.clj.Backend").getConstructor(ResourceLoader.class).newInstance(new Object[]{null});
    }
}
