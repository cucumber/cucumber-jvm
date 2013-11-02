package cucumber.runtime.java;

import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import cucumber.runtime.snippets.UnderscoreConcatenator;
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
    private final FunctionNameGenerator functionNameGenerator = new FunctionNameGenerator(new UnderscoreConcatenator());

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
                "@Given(\"^I have (\\\\d+) cukes in my \\\"([^\\\"]*)\\\" belly$\")\n" +
                "public void i_have_cukes_in_my_belly(int arg1, String arg2) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    @Test
    public void generatesCopyPasteReadyStepSnippetForNumberParameters() throws Exception {
        String expected = "" +
                "@Given(\"^before (\\\\d+) after$\")\n" +
                "public void before_after(int arg1) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        String snippet = snippetFor("before 5 after");
        assertEquals(expected, snippet);
    }

    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIllegalJavaIdentifierChars() {
        String expected = "" +
                "@Given(\"^I have (\\\\d+) cukes in: my \\\"([^\\\"]*)\\\" red-belly!$\")\n" +
                "public void i_have_cukes_in_my_red_belly(int arg1, String arg2) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 4 cukes in: my \"big\" red-belly!"));
    }


    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIntegersInsideStringParameter() {
        String expected = "" +
                "@Given(\"^the DI system receives a message saying \\\"([^\\\"]*)\\\"$\")\n" +
                "public void the_DI_system_receives_a_message_saying(String arg1) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("the DI system receives a message saying \"{ dataIngestion: { feeds: [ feed: { merchantId: 666, feedId: 1, feedFileLocation: feed.csv } ] }\""));
    }

    @Test
    public void generatesSnippetWithEscapedDollarSigns() {
        String expected = "" +
                "@Given(\"^I have \\\\$(\\\\d+)$\")\n" +
                "public void i_have_$(int arg1) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have $5"));
    }

    @Test
    public void generatesSnippetWithEscapedQuestionMarks() {
        String expected = "" +
                "@Given(\"^is there an error\\\\?:$\")\n" +
                "public void is_there_an_error() throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("is there an error?:"));
    }

    @Test
    public void generatesSnippetWithLotsOfEscapes() {
        String expected = "" +
                "@Given(\"^\\\\^\\\\(\\\\[a-z\\\\]\\\\*\\\\)\\\\?\\\\.\\\\+\\\\$$\")\n" +
                "public void a_z_$() throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("^([a-z]*)?.+$"));
    }

    @Test
    public void generatesSnippetWithEscapedParentheses() {
        String expected = "" +
                "@Given(\"^I have (\\\\d+) cukes \\\\(maybe more\\\\)$\")\n" +
                "public void i_have_cukes_maybe_more(int arg1) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 5 cukes (maybe more)"));
    }

    @Test
    public void generatesSnippetWithEscapedBrackets() {
        String expected = "" +
                "@Given(\"^I have (\\\\d+) cukes \\\\[maybe more\\\\]$\")\n" +
                "public void i_have_cukes_maybe_more(int arg1) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 5 cukes [maybe more]"));
    }

    @Test
    public void generatesSnippetWithDocString() {
        String expected = "" +
                "@Given(\"^I have:$\")\n" +
                "public void i_have(String arg1) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetForDocString("I have:", new DocString("text/plain", "hello", 1)));
    }

    @Test
    @Ignore
    public void recognisesWordWithNumbers() {
        String expected = "" +
                "@Given(\"^Then it responds ([^\\\"]*)$\")\n" +
                "public void Then_it_responds(String arg1) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "}\n";
        assertEquals(expected, snippetFor("Then it responds UTF-8"));
    }

    @Test
    public void generatesSnippetWithDataTable() {
        String expected = "" +
                "@Given(\"^I have:$\")\n" +
                "public void i_have(DataTable arg1) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    // For automatic conversion, change DataTable to List<YourType>\n" +
                "    throw new PendingException();\n" +
                "}\n";
        List<DataTableRow> dataTable = asList(new DataTableRow(NO_COMMENTS, asList("col1"), 1));
        assertEquals(expected, snippetForDataTable("I have:", dataTable));
    }

    private String snippetFor(String name) {
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, null, null);
        return new SnippetGenerator(new JavaSnippet()).getSnippet(step, functionNameGenerator);
    }

    private String snippetForDocString(String name, DocString docString) {
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, null, docString);
        return new SnippetGenerator(new JavaSnippet()).getSnippet(step, functionNameGenerator);
    }

    private String snippetForDataTable(String name, List<DataTableRow> dataTable) {
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, dataTable, null);
        return new SnippetGenerator(new JavaSnippet()).getSnippet(step, functionNameGenerator);
    }
}