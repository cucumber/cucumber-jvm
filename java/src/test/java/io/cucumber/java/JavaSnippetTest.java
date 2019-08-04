package io.cucumber.java;

import gherkin.pickles.Argument;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.cucumberexpressions.CaptureGroupTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.cucumberexpressions.TypeReference;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class JavaSnippetTest {

    private static final String GIVEN_KEYWORD = "Given";
    private final SnippetType snippetType = SnippetType.UNDERSCORE;

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
            "@Given(\"I have {int} cukes in my {string} belly\")\n" +
            "public void i_have_cukes_in_my_belly(Integer int1, String string) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetFor("I have 4 cukes in my \"big\" belly"), is(equalTo(expected)));
    }

    @Test
    public void generatesPlainSnippetUsingCustomParameterTypes() {
        ParameterType<Size> customParameterType = new ParameterType<Size>(
            "size",
            "small|medium|large",
            Size.class,
            new CaptureGroupTransformer<Size>() {
                @Override
                public Size transform(String... strings) {
                    return null;
                }
            }, true,
            false);

        String expected = "" +
            "@Given(\"I have {double} cukes in my {size} belly\")\n" +
            "public void i_have_cukes_in_my_belly(Double double1, Size size) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetFor("I have 4.2 cukes in my large belly", customParameterType), is(equalTo(expected)));
    }

    @Test
    public void generatesPlainSnippetUsingComplexParameterTypes() {
        ParameterType<List<Size>> customParameterType = new ParameterType<List<Size>>(
            "sizes",
            singletonList("(small|medium|large)(( and |, )(small|medium|large))*"),
            new TypeReference<List<Size>>() {
            }.getType(),
            new CaptureGroupTransformer<List<Size>>() {
                @Override
                public List<Size> transform(String... strings) {
                    return null;
                }
            },
            true,
            false);

        String expected = "" +
            "@Given(\"I have {sizes} bellies\")\n" +
            "public void i_have_bellies(java.util.List<io.cucumber.java.JavaSnippetTest$Size> sizes) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetFor("I have large and small bellies", customParameterType), is(equalTo(expected)));
    }

    @Test
    public void generatesCopyPasteReadyStepSnippetForNumberParameters() {
        String expected = "" +
            "@Given(\"before {int} after\")\n" +
            "public void before_after(Integer int1) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetFor("before 5 after"), is(equalTo(expected)));
    }

    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIllegalJavaIdentifierChars() {
        String expected = "" +
            "@Given(\"I have {int} cukes in: my {string} red-belly!\")\n" +
            "public void i_have_cukes_in_my_red_belly(Integer int1, String string) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetFor("I have 4 cukes in: my \"big\" red-belly!"), is(equalTo(expected)));
    }

    @Test
    public void generatesCopyPasteReadySnippetWhenStepHasIntegersInsideStringParameter() {
        String expected = "" +
            "@Given(\"the DI system receives a message saying {string}\")\n" +
            "public void the_DI_system_receives_a_message_saying(String string) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(expected, snippetFor("the DI system receives a message saying \"{ dataIngestion: { feeds: [ feed: { merchantId: 666, feedId: 1, feedFileLocation: feed.csv } ] }\""), is(equalTo(expected)));
    }

    @Test
    public void generatesSnippetWithDollarSigns() {
        String expected = "" +
            "@Given(\"I have ${int}\")\n" +
            "public void i_have_$(Integer int1) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetFor("I have $5"), is(equalTo(expected)));
    }

    @Test
    public void generatesSnippetWithQuestionMarks() {
        String expected = "" +
            "@Given(\"is there an error?:\")\n" +
            "public void is_there_an_error() {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetFor("is there an error?:"), is(equalTo(expected)));
    }

    @Test
    public void generatesSnippetWithLotsOfNonIdentifierCharacters() {
        String expected = "" +
            "@Given(\"\\\\([a-z]*)?.+\")\n" +
            "public void a_z() {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetFor("([a-z]*)?.+"), is(equalTo(expected)));
    }

    @Test
    public void generatesSnippetWithParentheses() {
        String expected = "" +
            "@Given(\"I have {int} cukes \\\\(maybe more)\")\n" +
            "public void i_have_cukes_maybe_more(Integer int1) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetFor("I have 5 cukes (maybe more)"), is(equalTo(expected)));
    }

    @Test
    public void generatesSnippetWithBrackets() {
        String expected = "" +
            "@Given(\"I have {int} cukes [maybe more]\")\n" +
            "public void i_have_cukes_maybe_more(Integer int1) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetFor("I have 5 cukes [maybe more]"), is(equalTo(expected)));
    }

    @Test
    public void generatesSnippetWithDocString() {
        String expected = "" +
            "@Given(\"I have:\")\n" +
            "public void i_have(String docString) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetForDocString("I have:", new PickleString(null, "hello")), is(equalTo(expected)));
    }

    @Test
    public void generatesSnippetWithMultipleArgumentsNamedDocString() {
        ParameterType<String> customParameterType = new ParameterType<String>(
            "docString",
            "\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"",
            String.class,
            new CaptureGroupTransformer<String>() {
                @Override
                public String transform(String... strings) {
                    return null;
                }
            },
            true,
            false);

        String expected = "" +
            "@Given(\"I have a {docString}:\")\n" +
            "public void i_have_a(String docString, String docString1) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n" +
            "\n" +
            "@Given(\"I have a {string}:\")\n" +
            "public void i_have_a(String string, String docString) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        assertThat(snippetForDocString("I have a \"Documentation String\":", new PickleString(null, "hello"), customParameterType), is(equalTo(expected)));
    }

    @Test
    @Disabled("TODO issue tracked to within io.cucumber.cucumberexpressions.CucumberExpressionGenerator")
    public void recognisesWordWithNumbers() {
        String expected = "" +
            "@Given(\"Then it responds ([\\\"]*)\")\n" +
            "public void Then_it_responds(String arg1) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "}\n";
        assertThat(snippetFor("Then it responds UTF-8"), is(equalTo(expected)));
    }

    @Test
    public void generatesSnippetWithDataTable() {
        String expected = "" +
            "@Given(\"I have:\")\n" +
            "public void i_have(io.cucumber.datatable.DataTable dataTable) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    // For automatic transformation, change DataTable to one of\n" +
            "    // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or\n" +
            "    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,\n" +
            "    // Double, Byte, Short, Long, BigInteger or BigDecimal.\n" +
            "    //\n" +
            "    // For other transformations you can register a DataTableType.\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        PickleTable dataTable = new PickleTable(asList(new PickleRow(asList(new PickleCell(null, "col1")))));
        assertThat(snippetForDataTable("I have:", dataTable), is(equalTo(expected)));
    }


    @Test
    public void generatesSnippetWithMultipleArgumentsNamedDataTable() {
        ParameterType<String> customParameterType = new ParameterType<String>(
            "dataTable",
            "\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"",
            String.class,
            new CaptureGroupTransformer<String>() {
                @Override
                public String transform(String... strings) {
                    return null;
                }
            },
            true,
            false);

        String expected = "" +
            "@Given(\"I have in table {dataTable}:\")\n" +
            "public void i_have_in_table(String dataTable, io.cucumber.datatable.DataTable dataTable1) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    // For automatic transformation, change DataTable to one of\n" +
            "    // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or\n" +
            "    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,\n" +
            "    // Double, Byte, Short, Long, BigInteger or BigDecimal.\n" +
            "    //\n" +
            "    // For other transformations you can register a DataTableType.\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n" +
            "\n" +
            "@Given(\"I have in table {string}:\")\n" +
            "public void i_have_in_table(String string, io.cucumber.datatable.DataTable dataTable) {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    // For automatic transformation, change DataTable to one of\n" +
            "    // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or\n" +
            "    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,\n" +
            "    // Double, Byte, Short, Long, BigInteger or BigDecimal.\n" +
            "    //\n" +
            "    // For other transformations you can register a DataTableType.\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";
        PickleTable dataTable = new PickleTable(asList(new PickleRow(asList(new PickleCell(null, "col1")))));
        assertThat(snippetForDataTable("I have in table \"M6\":", dataTable, customParameterType), is(equalTo(expected)));
    }

    @Test
    public void generateSnippetWithOutlineParam() {
        String expected = "" +
            "@Given(\"Then it responds <param>\")\n" +
            "public void then_it_responds_param() {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java.PendingException();\n" +
            "}\n";

        assertThat(snippetFor("Then it responds <param>"), is(equalTo(expected)));
    }

    private String snippetFor(String name) {
        PickleStep step = new PickleStep(name, Collections.<Argument>emptyList(), Collections.<PickleLocation>emptyList());
        List<String> snippet = new SnippetGenerator(new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH)).getSnippet(step, GIVEN_KEYWORD, snippetType);
        return String.join("\n", snippet);
    }


    private String snippetFor(String name, ParameterType<?> parameterType) {
        PickleStep step = new PickleStep(name, Collections.<Argument>emptyList(), Collections.<PickleLocation>emptyList());
        ParameterTypeRegistry parameterTypeRegistry = new ParameterTypeRegistry(Locale.ENGLISH);
        parameterTypeRegistry.defineParameterType(parameterType);
        List<String> snippet = new SnippetGenerator(new JavaSnippet(), parameterTypeRegistry).getSnippet(step, GIVEN_KEYWORD, snippetType);
        return String.join("\n", snippet);
    }

    private String snippetForDocString(String name, PickleString docString) {
        PickleStep step = new PickleStep(name, asList((Argument) docString), Collections.<PickleLocation>emptyList());
        List<String> snippet = new SnippetGenerator(new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH)).getSnippet(step, GIVEN_KEYWORD, snippetType);
        return String.join("\n", snippet);
    }


    private String snippetForDocString(String name, PickleString docString, ParameterType<String> parameterType) {
        PickleStep step = new PickleStep(name, asList((Argument) docString), Collections.<PickleLocation>emptyList());
        ParameterTypeRegistry parameterTypeRegistry = new ParameterTypeRegistry(Locale.ENGLISH);
        parameterTypeRegistry.defineParameterType(parameterType);
        List<String> snippet = new SnippetGenerator(new JavaSnippet(), parameterTypeRegistry).getSnippet(step, GIVEN_KEYWORD, snippetType);
        return String.join("\n", snippet);
    }


    private String snippetForDataTable(String name, PickleTable dataTable) {
        PickleStep step = new PickleStep(name, asList((Argument) dataTable), Collections.<PickleLocation>emptyList());
        List<String> snippet = new SnippetGenerator(new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH)).getSnippet(step, GIVEN_KEYWORD, snippetType);
        return String.join("\n", snippet);
    }


    private String snippetForDataTable(String name, PickleTable dataTable, ParameterType<String> parameterType) {
        PickleStep step = new PickleStep(name, asList((Argument) dataTable), Collections.<PickleLocation>emptyList());
        ParameterTypeRegistry parameterTypeRegistry = new ParameterTypeRegistry(Locale.ENGLISH);
        parameterTypeRegistry.defineParameterType(parameterType);
        List<String> snippet = new SnippetGenerator(new JavaSnippet(), parameterTypeRegistry).getSnippet(step, GIVEN_KEYWORD, snippetType);
        return String.join("\n", snippet);
    }

    private static class Size {
        // Dummy. Makes the test readable
    }

}
