package io.cucumber.java8;

import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTable;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class Java8SnippetTest {

    private static final String GIVEN_KEYWORD = "Given";

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
            "Given(\"I have {int} cukes in my {string} belly\", (Integer int1, String string) -> {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java8.PendingException();\n" +
            "});\n";
        assertThat(plainSnippet(), is(equalTo(expected)));
    }

    @Test
    public void generatesDataTableSnippet() {
        String expected = "" +
            "Given(\"I have {int} cukes in my {string} belly\", (Integer int1, String string, io.cucumber.datatable.DataTable dataTable) -> {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    // For automatic transformation, change DataTable to one of\n" +
            "    // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or\n" +
            "    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,\n" +
            "    // Double, Byte, Short, Long, BigInteger or BigDecimal.\n" +
            "    //\n" +
            "    // For other transformations you can register a DataTableType.\n" +
            "    throw new io.cucumber.java8.PendingException();\n" +
            "});\n";
        assertThat(dataTableSnippet(), is(equalTo(expected)));
    }

    private String plainSnippet() {
        return snippet2(Collections.emptyList());
    }

    private String dataTableSnippet() {
        return snippet2(Collections.singletonList(new PickleTable(Collections.emptyList())));
    }

    private String snippet2(List<Argument> arguments) {
        PickleStep step = new PickleStep("I have 4 cukes in my \"big\" belly", arguments, Collections.emptyList());
        return String.join(
            "\n",
            new SnippetGenerator(
                new Java8Snippet(),
                new ParameterTypeRegistry(Locale.ENGLISH)).getSnippet(step, GIVEN_KEYWORD, SnippetType.UNDERSCORE)
        );
    }

}
