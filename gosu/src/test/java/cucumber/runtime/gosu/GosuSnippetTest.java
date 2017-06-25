package cucumber.runtime.gosu;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class GosuSnippetTest {
    private static final String GIVEN_KEYWORD = "Given";

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
        assertEquals(expected, snippetForDocString("I have:", new PickleString(null, "hello")));
    }

    @Test
    public void generatesSnippetWithDataTable() {
        String expected = "" +
                "Dsl.Given('^I have:$', \\ arg1 : DataTable  -> {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "})\n";
        PickleTable dataTable = new PickleTable(asList(new PickleRow(asList(new PickleCell(null, "col1")))));
        assertEquals(expected, snippetForDataTable("I have:", dataTable));
    }

    private String snippetFor(String name) {
        PickleStep step = new PickleStep(name, Collections.emptyList(), Collections.emptyList());
        return new SnippetGenerator(new GosuSnippet()).getSnippet(step, GIVEN_KEYWORD, null);
    }

    private String snippetForDocString(String name, PickleString docString) {
        PickleStep step = new PickleStep(name, asList((Argument)docString), Collections.emptyList());
        return new SnippetGenerator(new GosuSnippet()).getSnippet(step, GIVEN_KEYWORD, null);
    }

    private String snippetForDataTable(String name, PickleTable dataTable) {
        PickleStep step = new PickleStep(name, asList((Argument)dataTable), Collections.emptyList());
        return new SnippetGenerator(new GosuSnippet()).getSnippet(step, GIVEN_KEYWORD, null);
    }
}
