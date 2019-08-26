package cucumber.runtime.gosu;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class GosuSnippetTest {

    private static final List<Comment> NO_COMMENTS = Collections.emptyList();

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
                "Dsl.Given('^I have (\\\\d+) cukes in my \"([^\"]*)\" belly$', \\ arg1 : int , arg2 : String  -> {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "})\n";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    @Test
    public void generatesCopyPasteReadyStepSnippetForNumberParameters() throws Exception {
        String expected = "" +
                "Dsl.Given('^before (\\\\d+) after$', \\ arg1 : int  -> {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "})\n";
        String snippet = snippetFor("before 5 after");
        assertEquals(expected, snippet);
    }

    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIllegalJavaIdentifierChars() {
        String expected = "" +
                "Dsl.Given('^I have (\\\\d+) cukes in: my \"([^\"]*)\" red-belly!$', \\ arg1 : int , arg2 : String  -> {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "})\n";
        assertEquals(expected, snippetFor("I have 4 cukes in: my \"big\" red-belly!"));
    }


    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIntegersInsideStringParameter() {
        String expected = "" +
                "Dsl.Given('^the DI system receives a message saying \"([^\"]*)\"$', \\ arg1 : String  -> {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "})\n";
        assertEquals(expected, snippetFor("the DI system receives a message saying \"{ dataIngestion: { feeds: [ feed: { merchantId: 666, feedId: 1, feedFileLocation: feed.csv } ] }\""));
    }

    @Test
    public void generatesSnippetWithDocString() {
        String expected = "" +
                "Dsl.Given('^I have:$', \\ arg1 : String  -> {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "})\n";
        assertEquals(expected, snippetForDocString("I have:", new DocString("text/plain", "hello", 1)));
    }

    @Test
    public void generatesSnippetWithDataTable() {
        String expected = "" +
                "Dsl.Given('^I have:$', \\ arg1 : DataTable  -> {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "})\n";
        List<DataTableRow> dataTable = asList(new DataTableRow(NO_COMMENTS, asList("col1"), 1));
        assertEquals(expected, snippetForDataTable("I have:", dataTable));
    }

    private String snippetFor(String name) {
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, null, null);
        return new SnippetGenerator(new GosuSnippet()).getSnippet(step, null);
    }

    private String snippetForDocString(String name, DocString docString) {
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, null, docString);
        return new SnippetGenerator(new GosuSnippet()).getSnippet(step, null);
    }

    private String snippetForDataTable(String name, List<DataTableRow> dataTable) {
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, dataTable, null);
        return new SnippetGenerator(new GosuSnippet()).getSnippet(step, null);
    }
}
