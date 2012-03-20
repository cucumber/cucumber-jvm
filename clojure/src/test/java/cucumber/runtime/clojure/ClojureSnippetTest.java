package cucumber.runtime.clojure;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ClojureSnippetTest {
    private static final List<Comment> NO_COMMENTS = Collections.emptyList();

    @Test
    public void generatesPlainSnippet() {
        Step step = new Step(NO_COMMENTS, "Given ", "I have 4 cukes in my \"big\" belly", 0, null, null);
        String snippet = new SnippetGenerator(new ClojureSnippet()).getSnippet(step);
        String expected = "" +
                "(Given #\"^I have (\\d+) cukes in my \"([^\"]*)\" belly$\" [arg1, arg2]\n" +
                "  (comment  Express the Regexp above with the code you wish you had  ))\n";
        assertEquals(expected, snippet);
    }

    @Test
    public void generatesSnippetWithDataTable() {
        List<DataTableRow> dataTable = asList(new DataTableRow(NO_COMMENTS, asList("col1"), 1));
        Step step = new Step(NO_COMMENTS, "Given ", "I have:", 0, dataTable, null);
        String snippet = new SnippetGenerator(new ClojureSnippet()).getSnippet(step);
        String expected = "" +
                "(Given #\"^I have:$\" [arg1]\n" +
                "  (comment  Express the Regexp above with the code you wish you had  ))\n";
        assertEquals(expected, snippet);
    }
}
