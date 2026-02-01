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

    private final SnippetGenerator snippetGenerator = new SnippetGenerator("en", new Java8Snippet(),
        new ParameterTypeRegistry(Locale.ENGLISH));

    @Test
    void generatesPlainSnippet() {
        Feature feature = TestFeatureParser.parse("""
                Feature: Test feature
                  Scenario: Test scenario
                     Given I have 4 cukes in my "big" belly
                """);
        String expected = """
                Given("I have {int} cukes in my {string} belly", (Integer int1, String string) -> {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java8.PendingException();
                });""";
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
        Feature feature = TestFeatureParser.parse("""
                Feature: Test feature
                  Scenario: Test scenario
                     Given I have 4 cukes in my "big" belly
                      | data table cell |
                """);

        String expected = """
                Given("I have {int} cukes in my {string} belly", (Integer int1, String string, io.cucumber.datatable.DataTable dataTable) -> {
                    // Write code here that turns the phrase above into concrete actions
                    // For automatic transformation, change DataTable to one of
                    // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or
                    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,
                    // Double, Byte, Short, Long, BigInteger or BigDecimal.
                    //
                    // For other transformations you can register a DataTableType.
                    throw new io.cucumber.java8.PendingException();
                });""";
        assertThat(getSnippet(feature), is(equalTo(expected)));
    }

}
