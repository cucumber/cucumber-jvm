package io.cucumber.java8;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

class Java8SnippetTest {

    private final SnippetGenerator snippetGenerator = new SnippetGenerator(
        new Java8Snippet(),
        new ParameterTypeRegistry(Locale.ENGLISH));

    @Test
    void generatesPlainSnippet() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my \"big\" belly\n");
        String expected = "" +
                "Given(\"I have {int} cukes in my {string} belly\", (Integer int1, String string) -> {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new io.cucumber.java8.PendingException();\n" +
                "});";
        assertThat(getSnippet(feature), is(equalTo(expected)));
    }

    private String getSnippet(Feature feature) {
        Step step = feature.getPickles().get(0).getSteps().get(0);
        return String.join(
            "\n",
            snippetGenerator.getSnippet(step, SnippetType.UNDERSCORE));
    }

    @Test
    void generatesDataTableSnippet() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my \"big\" belly\n" +
                "      | data table cell | \n");

        String expected = "" +
                "Given(\"I have {int} cukes in my {string} belly\", (Integer int1, String string, io.cucumber.datatable.DataTable dataTable) -> {\n"
                +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    // For automatic transformation, change DataTable to one of\n" +
                "    // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or\n" +
                "    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,\n" +
                "    // Double, Byte, Short, Long, BigInteger or BigDecimal.\n" +
                "    //\n" +
                "    // For other transformations you can register a DataTableType.\n" +
                "    throw new io.cucumber.java8.PendingException();\n" +
                "});";
        assertThat(getSnippet(feature), is(equalTo(expected)));
    }

}
