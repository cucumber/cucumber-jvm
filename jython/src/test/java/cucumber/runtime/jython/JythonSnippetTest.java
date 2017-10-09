package cucumber.runtime.jython;

import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import cucumber.runtime.snippets.UnderscoreConcatenator;
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

public class JythonSnippetTest {
    private static final List<Argument> NO_ARGUMENTS = Collections.emptyList();
    private static final List<PickleLocation> NO_LOCATIONS = Collections.emptyList();

    @Test
    public void generatesSnippetWithTwoArgs() {
        String expected = "" +
                "@Given('^I have (\\d+) cukes in my \"([^\"]*)\" belly$')\n" +
                "def i_have_cukes_in_my_belly(self, arg1, arg2):\n" +
                "  # Write code here that turns the phrase above into concrete actions\n" +
                "  raise(PendingException())\n" +
                "";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    @Test
    public void generatesSnippetWithZeroArgs() {
        String expected = "" +
                "@Given('^I have no cukes belly$')\n" +
                "def i_have_no_cukes_belly(self):\n" +
                "  # Write code here that turns the phrase above into concrete actions\n" +
                "  raise(PendingException())\n" +
                "";
        assertEquals(expected, snippetFor("I have no cukes belly"));
    }

    @Test
    public void generatesSnippetWithDataTable() {
        String expected = "" +
                "@Given('^I have:$')\n" +
                "def i_have(self, arg1):\n" +
                "  # Write code here that turns the phrase above into concrete actions\n" +
                "  # The last argument is a List of List of String\n" +
                "  raise(PendingException())\n" +
                "";
        PickleTable dataTable = new PickleTable(asList(new PickleRow(asList(new PickleCell(null, "col1")))));
        assertEquals(expected, snippetForDataTable("I have:", asList((Argument)dataTable)));
    }

    private String snippetFor(String name) {
        PickleStep step = new PickleStep(name, NO_ARGUMENTS, NO_LOCATIONS);
        return new SnippetGenerator(new JythonSnippet()).getSnippet(step, "Given", new FunctionNameGenerator(new UnderscoreConcatenator()));
    }

    private String snippetForDataTable(String name, List<Argument> dataTable) {
        PickleStep step = new PickleStep(name, dataTable, NO_LOCATIONS);
        return new SnippetGenerator(new JythonSnippet()).getSnippet(step, "Given", new FunctionNameGenerator(new UnderscoreConcatenator()));
    }
}
