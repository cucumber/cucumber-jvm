package cucumber.runtime.jython;

import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import cucumber.runtime.snippets.UnderscoreConcatenator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class JythonSnippetTest {
    private static final List<Comment> NO_COMMENTS = Collections.emptyList();

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
        List<DataTableRow> dataTable = asList(new DataTableRow(NO_COMMENTS, asList("col1"), 1));
        assertEquals(expected, snippetForDataTable("I have:", dataTable));
    }

    private String snippetFor(String name) {
        Step step = new Step(Collections.<Comment>emptyList(), "Given ", name, 0, null, null);
        return new SnippetGenerator(new JythonSnippet()).getSnippet(step, new FunctionNameGenerator(new UnderscoreConcatenator()));
    }

    private String snippetForDataTable(String name, List<DataTableRow> dataTable) {
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, dataTable, null);
        return new SnippetGenerator(new JythonSnippet()).getSnippet(step, new FunctionNameGenerator(new UnderscoreConcatenator()));
    }
}