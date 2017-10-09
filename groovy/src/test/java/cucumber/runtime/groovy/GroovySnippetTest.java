package cucumber.runtime.groovy;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class GroovySnippetTest {

    private static final List<Argument> NO_ARGUMENTS = Collections.emptyList();
    private static final List<PickleLocation> NO_LOCATIONS = Collections.emptyList();

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
                "Given(~/^I have (\\d+) cukes in my \"([^\"]*)\" belly$/) { int arg1, String arg2 ->\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    @Test
    public void generatesCopyPasteReadyStepSnippetForNumberParameters() throws Exception {
        String expected = "" +
                "Given(~/^before (\\d+) after$/) { int arg1 ->\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "}\n";
        String snippet = snippetFor("before 5 after");
        assertEquals(expected, snippet);
    }

    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIllegalJavaIdentifierChars() {
        String expected = "" +
                "Given(~/^I have (\\d+) cukes in: my \"([^\"]*)\" red-belly!$/) { int arg1, String arg2 ->\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 4 cukes in: my \"big\" red-belly!"));
    }


    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIntegersInsideStringParameter() {
        String expected = "" +
                "Given(~/^the DI system receives a message saying \"([^\"]*)\"$/) { String arg1 ->\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "}\n";
        assertEquals(expected, snippetFor("the DI system receives a message saying \"{ dataIngestion: { feeds: [ feed: { merchantId: 666, feedId: 1, feedFileLocation: feed.csv } ] }\""));
    }

    @Test
    public void generatesSnippetWithEscapedDollarSigns() {
        String expected = "" +
                "Given(~/^I have \\$(\\d+)$/) { int arg1 ->\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have $5"));
    }

    @Test
    public void generatesSnippetWithEscapedParentheses() {
        String expected = "" +
                "Given(~/^I have (\\d+) cukes \\(maybe more\\)$/) { int arg1 ->\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 5 cukes (maybe more)"));
    }

    @Test
    public void generatesSnippetWithEscapedBrackets() {
        String expected = "" +
                "Given(~/^I have (\\d+) cukes \\[maybe more\\]$/) { int arg1 ->\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 5 cukes [maybe more]"));
    }

    @Test
    public void generatesSnippetWithDocString() {
        String expected = "" +
                "Given(~/^I have:$/) { String arg1 ->\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "}\n";
        assertEquals(expected, snippetForDocString("I have:", new PickleString(null, "hello")));
    }

    @Test
    public void generatesSnippetWithDataTable() {
        String expected = "" +
                "Given(~/^I have:$/) { DataTable arg1 ->\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "}\n";
        PickleTable dataTable = new PickleTable(asList(new PickleRow(asList(new PickleCell(null, "col1")))));
        assertEquals(expected, snippetForDataTable("I have:", dataTable));
    }

    @Test
    public void generateSnippetWithEscapedEscapeCharacter() {
        String expected = "" +
                "Given(~/^I have (\\d+) cukes in my belly$/) { int arg1 ->\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException()\n" +
                "}\n";

        assertEquals(expected, snippetFor("I have 4 cukes in my belly"));
    }

    private String snippetFor(String name) {
        PickleStep step = new PickleStep(name, NO_ARGUMENTS, NO_LOCATIONS);
        return new SnippetGenerator(new GroovySnippet()).getSnippet(step, "Given", null);
    }

    private String snippetForDocString(String name, PickleString docString) {
        PickleStep step = new PickleStep(name, asList((Argument) docString), NO_LOCATIONS);
        return new SnippetGenerator(new GroovySnippet()).getSnippet(step, "Given", null);
    }

    private String snippetForDataTable(String name, PickleTable dataTable) {
        PickleStep step = new PickleStep(name, asList((Argument) dataTable), NO_LOCATIONS);
        return new SnippetGenerator(new GroovySnippet()).getSnippet(step, "Given", null);
    }
}
