package cucumber.runtime.java;

import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import cucumber.runtime.snippets.UnderscoreConcatenator;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class JavaSnippetTest {

    private static final String GIVEN_KEYWORD = "Given";
    private final FunctionNameGenerator functionNameGenerator = new FunctionNameGenerator(new UnderscoreConcatenator());

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
                "@Given(\"I have {int} cukes in my {string} belly\")\n" +
                "public void i_have_cukes_in_my_belly(int arg1, String arg2) {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    @Test
    public void generatesCopyPasteReadyStepSnippetForNumberParameters() {
        String expected = "" +
                "@Given(\"before {int} after\")\n" +
                "public void before_after(int arg1) {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        String snippet = snippetFor("before 5 after");
        assertEquals(expected, snippet);
    }

    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIllegalJavaIdentifierChars() {
        String expected = "" +
                "@Given(\"I have {int} cukes in: my {string} red-belly!\")\n" +
                "public void i_have_cukes_in_my_red_belly(int arg1, String arg2) {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 4 cukes in: my \"big\" red-belly!"));
    }

    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIntegersInsideStringParameter() {
        String expected = "" +
                "@Given(\"the DI system receives a message saying {string}\")\n" +
                "public void the_DI_system_receives_a_message_saying(String arg1) {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("the DI system receives a message saying \"{ dataIngestion: { feeds: [ feed: { merchantId: 666, feedId: 1, feedFileLocation: feed.csv } ] }\""));
    }

    @Test
    @Ignore //TODO: no longer relevant for cucumber-expressions
    public void generatesSnippetWithEscapedDollarSigns() {
        String expected = "" +
                "@Given(\"I have ${int}\")\n" +
                "public void i_have_$(int arg1) throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have $5"));
    }

    @Test
    @Ignore //TODO: no longer relevant for cucumber-expressions
    public void generatesSnippetWithEscapedQuestionMarks() {
        String expected = "" +
                "@Given(\"is there an error?:\")\n" +
                "public void is_there_an_error() throws Throwable {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("is there an error?:"));
    }

    @Test
    @Ignore //TODO: no longer relevant for cucumber-expressions
    public void generatesSnippetWithLotsOfEscapes() {
        String expected = "" +
                "@Given(\"\\\\\\\\(\\\\[a-z\\\\]\\\\*\\\\)\\\\?\\\\.\\\\+\\\\\")\n" +
                "public void a_z_() {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("([a-z]*)?.+"));
    }

    @Test
    @Ignore //TODO: no longer relevant for cucumber-expressions
    public void generatesSnippetWithEscapedParentheses() {
        String expected = "" +
                "@Given(\"I have {int} cukes (maybe more)\")\n" +
                "public void i_have_cukes_maybe_more(int arg1) {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 5 cukes (maybe more)"));
    }

    @Test
    @Ignore //TODO: no longer relevant for cucumber-expressions
    public void generatesSnippetWithEscapedBrackets() {
        String expected = "" +
                "@Given(\"I have {int} cukes [maybe more]\")\n" +
                "public void i_have_cukes_maybe_more(int arg1) {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetFor("I have 5 cukes [maybe more]"));
    }

    @Test
    public void generatesSnippetWithDocString() {
        String expected = "" +
                "@Given(\"I have:\")\n" +
                "public void i_have(String arg1) {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";
        assertEquals(expected, snippetForDocString("I have:", new PickleString(null, "hello")));
    }

    @Test
    @Ignore
    public void recognisesWordWithNumbers() {
        String expected = "" +
                "@Given(\"Then it responds ([\\\"]*)\")\n" +
                "public void Then_it_responds(String arg1) {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "}\n";
        assertEquals(expected, snippetFor("Then it responds UTF-8"));
    }

    @Test
    public void generatesSnippetWithDataTable() {
        String expected = "" +
                "@Given(\"I have:\")\n" +
                "public void i_have(DataTable arg1) {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    // For automatic transformation, change DataTable to one of\n" +
                "    // List<YourType>, List<List<E>>, List<Map<K,V>> or Map<K,V>.\n" +
                "    // E,K,V must be a scalar (String, Integer, Date, enum etc).\n" +
                "    // Field names for YourType must match the column names in \n" +
                "    // your feature file (except for spaces and capitalization).\n" +
                "    throw new PendingException();\n" +
                "}\n";
        PickleTable dataTable = new PickleTable(asList(new PickleRow(asList(new PickleCell(null, "col1")))));
        assertEquals(expected, snippetForDataTable("I have:", dataTable));
    }

    @Test
    @Ignore //TODO: Fix for cucumber-expressions
    public void generateSnippetWithOutlineParam() {
        String expected = "" +
                "@Given(\"Then it responds <param>\")\n" +
                "public void then_it_responds_param() {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "}\n";

        assertEquals(expected, snippetFor("Then it responds <param>"));
    }

    private String snippetFor(String name) {
        PickleStep step = new PickleStep(name, Collections.<Argument>emptyList(), Collections.<PickleLocation>emptyList());
        return new SnippetGenerator(new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH)).getSnippet(step, GIVEN_KEYWORD, functionNameGenerator);
    }

    private String snippetForDocString(String name, PickleString docString) {
        PickleStep step = new PickleStep(name, asList((Argument)docString), Collections.<PickleLocation>emptyList());
        return new SnippetGenerator(new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH)).getSnippet(step, GIVEN_KEYWORD, functionNameGenerator);
    }

    private String snippetForDataTable(String name, PickleTable dataTable) {
        PickleStep step = new PickleStep(name, asList((Argument)dataTable), Collections.<PickleLocation>emptyList());
        return new SnippetGenerator(new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH)).getSnippet(step, GIVEN_KEYWORD, functionNameGenerator);
    }
}