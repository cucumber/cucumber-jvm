package cucumber.runtime.java;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Step;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class JavaSnippetTest {

    private static final List<Comment> NO_COMMENTS = Collections.emptyList();

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
                "@Given(\"^I have (\\\\d+) cukes in my \\\"([^\\\"]*)\\\" belly$\")\n" +
                "public void iHaveCukesInMyBelly(int arg1, String arg2) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    @Test
    public void generatesCopyPasteReadyStepSnippetForNumberParameters() throws Exception {
        String expected = "" +
                "@Given(\"^before (\\\\d+) after$\")\n" +
                "public void beforeAfter(int arg1) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "    throw new PendingException();\n" +
                "}\n";
        String snippet = snippetFor("before 5 after");
        assertEquals(expected, snippet);
    }

    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIllegalJavaIdentifierChars() {
        String expected = "" +
                "@Given(\"^I have (\\\\d+) cukes in: my \\\"([^\\\"]*)\\\" red-belly!$\")\n" +
                "public void iHaveCukesInMyRedBelly(int arg1, String arg2) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 4 cukes in: my \"big\" red-belly!"));
    }


    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIntegersInsideStringParameter() {
        String expected = "" +
                "@Given(\"^the DI system receives a message saying \\\"([^\\\"]*)\\\"$\")\n" +
                "public void theDiSystemReceivesAMessageSaying(String arg1) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("the DI system receives a message saying \"{ dataIngestion: { feeds: [ feed: { merchantId: 666, feedId: 1, feedFileLocation: feed.csv } ] }\""));
    }

    @Test
    public void generatesSnippetWithEscapedDollarSigns() {
        String expected = "" +
                "@Given(\"^I have \\\\$(\\\\d+)$\")\n" +
                "public void iHave$(int arg1) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have $5"));
    }

    @Test
    public void generatesSnippetWithEscapedParentheses() {
        String expected = "" +
                "@Given(\"^I have (\\\\d+) cukes \\\\(maybe more\\\\)$\")\n" +
                "public void iHaveCukesMaybeMore(int arg1) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 5 cukes (maybe more)"));
    }

    @Test
    public void generatesSnippetWithEscapedBrackets() {
        String expected = "" +
                "@Given(\"^I have (\\\\d+) cukes \\\\[maybe more\\\\]$\")\n" +
                "public void iHaveCukesMaybeMore(int arg1) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 5 cukes [maybe more]"));
    }

    @Test
    public void generatesSnippetWithDocString() {
        String expected = "" +
                "@Given(\"^I have:$\")\n" +
                "public void iHave(String arg1) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetForDocString("I have:", new DocString("text/plain", "hello", 1)));
    }

    @Test
    @Ignore
    public void recognisesWordWithNumbers() {
        String expected = "" +
                "@Given(\"^Then it responds ([^\\\"]*)$\")\n" +
                "public void thenItRespondsUTF(int arg1) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "}\n";
        assertEquals(expected, snippetFor("Then it responds UTF-8"));
    }

    @Test
    public void generatesSnippetWithDataTable() {
        String expected = "" +
                "@Given(\"^I have:$\")\n" +
                "public void iHave(DataTable arg1) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "    // For automatic conversion, change DataTable to List<YourType>\n" +
                "    throw new PendingException();\n" +
                "}\n";
        List<DataTableRow> dataTable = asList(new DataTableRow(NO_COMMENTS, asList("col1"), 1));
        assertEquals(expected, snippetForDataTable("I have:", dataTable));
    }

    private String snippetFor(String name) {
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, null, null);
        return new SnippetGenerator(new JavaSnippet()).getSnippet(step);
    }

    private String snippetForDocString(String name, DocString docString) {
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, null, docString);
        return new SnippetGenerator(new JavaSnippet()).getSnippet(step);
    }

    private String snippetForDataTable(String name, List<DataTableRow> dataTable) {
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, dataTable, null);
        return new SnippetGenerator(new JavaSnippet()).getSnippet(step);
    }
}