package io.cucumber.java;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.cucumberexpressions.TypeReference;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

class JavaSnippetTest {

    private final SnippetType snippetType = SnippetType.UNDERSCORE;

    @Test
    void generatesPlainSnippet() {
        String expected = """
                @Given("I have {int} cukes in my {string} belly")
                public void i_have_cukes_in_my_belly(Integer int1, String string) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("I have 4 cukes in my \"big\" belly"), is(equalTo(expected)));
    }

    private String snippetFor(String stepText) {
        Step step = createStep(stepText);
        List<String> snippet = new SnippetGenerator("en", new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH))
                .getSnippet(step, snippetType);
        return String.join("\n", snippet);
    }

    private Step createStep(String stepText) {
        String source = """
                Feature: Test feature
                  Scenario: Test Scenario
                    Given %s
                """.formatted(stepText);

        Feature feature = TestFeatureParser.parse(source);
        return feature.getPickles().get(0).getSteps().get(0);
    }

    @Test
    void generatesPlainSnippetUsingCustomParameterTypes() {
        ParameterType<Size> customParameterType = new ParameterType<Size>(
            "size",
            "small|medium|large",
            Size.class,
            (String... groups) -> null,
            true,
            false);

        String expected = """
                @Given("I have {double} cukes in my {size} belly")
                public void i_have_cukes_in_my_belly(Double double1, Size size) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("I have 4.2 cukes in my large belly", customParameterType), is(equalTo(expected)));
    }

    @Test
    void generatesSnippetsWithValidJavaIdentifiers() {
        ParameterType<Size> customParameterType = new ParameterType<Size>(
            "small-size",
            "tiny|small|medium",
            Size.class,
            (String... groups) -> null,
            true,
            false);

        String expected = """
                @Given("I have {double} cukes in my {small-size} belly")
                public void i_have_cukes_in_my_belly(Double double1, Size smallSize) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("I have 4.2 cukes in my tiny belly", customParameterType), is(equalTo(expected)));
    }

    @Test
    void generatesSnippetsWithNonEmptyMethodNames() {
        ParameterType<Size> customParameterType = new ParameterType<Size>(
            "small-size",
            "tiny|small|medium",
            Size.class,
            (String... groups) -> null,
            true,
            false);

        String expected = """
                @Given("{double} {small-size}")
                public void double_small_size(Double double1, Size smallSize) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("4.2 medium", customParameterType), is(equalTo(expected)));
    }

    private String snippetFor(String stepText, ParameterType<?> parameterType) {
        Step step = createStep(stepText);
        ParameterTypeRegistry parameterTypeRegistry = new ParameterTypeRegistry(Locale.ENGLISH);
        parameterTypeRegistry.defineParameterType(parameterType);
        List<String> snippet = new SnippetGenerator("en", new JavaSnippet(), parameterTypeRegistry).getSnippet(step,
            snippetType);
        return String.join("\n", snippet);
    }

    @Test
    void generatesPlainSnippetUsingComplexParameterTypes() {
        ParameterType<List<Size>> customParameterType = new ParameterType<>(
            "sizes",
            singletonList("(small|medium|large)(( and |, )(small|medium|large))*"),
            new TypeReference<List<Size>>() {
            }.getType(),
            (String[] groups) -> null,
            true,
            false);

        String expected = """
                @Given("I have {sizes} bellies")
                public void i_have_bellies(java.util.List<io.cucumber.java.JavaSnippetTest$Size> sizes) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("I have large and small bellies", customParameterType), is(equalTo(expected)));
    }

    @Test
    void generatesCopyPasteReadyStepSnippetForNumberParameters() {
        String expected = """
                @Given("before {int} after")
                public void before_after(Integer int1) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("before 5 after"), is(equalTo(expected)));
    }

    @Test
    void generatesCopyPasteReadySnippetWhenStepHasIllegalJavaIdentifierChars() {
        String expected = """
                @Given("I have {int} cukes in: my {string} red-belly!")
                public void i_have_cukes_in_my_red_belly(Integer int1, String string) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("I have 4 cukes in: my \"big\" red-belly!"), is(equalTo(expected)));
    }

    @Test
    void generatesCopyPasteReadySnippetWhenStepHasIntegersInsideStringParameter() {
        String expected = """
                @Given("the DI system receives a message saying {string}")
                public void the_di_system_receives_a_message_saying(String string) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(expected, snippetFor(
            "the DI system receives a message saying \"{ dataIngestion: { feeds: [ feed: { merchantId: 666, feedId: 1, feedFileLocation: feed.csv } ] }\""),
            is(equalTo(expected)));
    }

    @Test
    void generatesSnippetWithQuestionMarks() {
        String expected = """
                @Given("is there an error?:")
                public void is_there_an_error() {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("is there an error?:"), is(equalTo(expected)));
    }

    @Test
    void generatesSnippetWithLotsOfNonIdentifierCharacters() {
        String expected = """
                @Given("\\\\([a-z]*)?.+")
                public void a_z() {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("([a-z]*)?.+"), is(equalTo(expected)));
    }

    @Test
    void generatesSnippetWithParentheses() {
        String expected = """
                @Given("I have {int} cukes \\\\(maybe more)")
                public void i_have_cukes_maybe_more(Integer int1) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("I have 5 cukes (maybe more)"), is(equalTo(expected)));
    }

    @Test
    void generatesSnippetWithBrackets() {
        String expected = """
                @Given("I have {int} cukes [maybe more]")
                public void i_have_cukes_maybe_more(Integer int1) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetFor("I have 5 cukes [maybe more]"), is(equalTo(expected)));
    }

    @Test
    void generatesSnippetWithDocString() {
        String expected = """
                @Given("I have:")
                public void i_have(String docString) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetForDocString("I have:", "hello"), is(equalTo(expected)));
    }

    private String snippetForDocString(String stepText, String docString) {
        Step step = createStepWithDocString(stepText, docString);
        List<String> snippet = new SnippetGenerator("en", new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH))
                .getSnippet(step, snippetType);
        return String.join("\n", snippet);
    }

    private Step createStepWithDocString(String stepText, String docString) {
        String source = """
                Feature: Test feature
                  Scenario: Test Scenario
                    Given %s
                      ""\"
                      %s
                      ""\"""".formatted(stepText, docString);

        Feature feature = TestFeatureParser.parse(source);
        return feature.getPickles().get(0).getSteps().get(0);
    }

    @Test
    void generatesSnippetWithMultipleArgumentsNamedDocString() {
        ParameterType<String> customParameterType = new ParameterType<>(
            "docString",
            "\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"",
            String.class,
            (String[] groups) -> null,
            true,
            false);

        String expected = """
                @Given("I have a {docString}:")
                public void i_have_a(String docString, String docString1) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }
                @Given("I have a {string}:")
                public void i_have_a(String string, String docString) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetForDocString("I have a \"Documentation String\":", "hello", customParameterType),
            is(equalTo(expected)));
    }

    private String snippetForDocString(String stepText, String docString, ParameterType<String> parameterType) {
        Step step = createStepWithDocString(stepText, docString);
        ParameterTypeRegistry parameterTypeRegistry = new ParameterTypeRegistry(Locale.ENGLISH);
        parameterTypeRegistry.defineParameterType(parameterType);
        List<String> snippet = new SnippetGenerator("en", new JavaSnippet(), parameterTypeRegistry).getSnippet(step,
            snippetType);
        return String.join("\n", snippet);
    }

    @Test
    @Disabled("TODO issue tracked to within io.cucumber.cucumberexpressions.CucumberExpressionGenerator")
    void recognisesWordWithNumbers() {
        String expected = """
                @Given("Then it responds ([\\"]*)")
                public void Then_it_responds(String arg1) {
                    // Write code here that turns the phrase above into concrete actions
                }""";
        assertThat(snippetFor("Then it responds UTF-8"), is(equalTo(expected)));
    }

    @Test
    void generatesSnippetWithDataTable() {
        String expected = """
                @Given("I have:")
                public void i_have(io.cucumber.datatable.DataTable dataTable) {
                    // Write code here that turns the phrase above into concrete actions
                    // For automatic transformation, change DataTable to one of
                    // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or
                    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,
                    // Double, Byte, Short, Long, BigInteger or BigDecimal.
                    //
                    // For other transformations you can register a DataTableType.
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetForDataTable("I have:"), is(equalTo(expected)));
    }

    private String snippetForDataTable(String stepText) {
        Step step = createStepWithDataTable(stepText);
        List<String> snippet = new SnippetGenerator("en", new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH))
                .getSnippet(step, snippetType);
        return String.join("\n", snippet);
    }

    private Step createStepWithDataTable(String stepText) {
        String source = """
                Feature: Test feature
                  Scenario: Test Scenario
                    Given %s
                      | key   |
                      | value |
                """.formatted(stepText);

        Feature feature = TestFeatureParser.parse(source);
        return feature.getPickles().get(0).getSteps().get(0);
    }

    @Test
    void generatesSnippetWithMultipleArgumentsNamedDataTable() {
        ParameterType<String> customParameterType = new ParameterType<>(
            "dataTable",
            "\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"",
            String.class,
            (String[] groups) -> null,
            true,
            false);

        String expected = """
                @Given("I have in table {dataTable}:")
                public void i_have_in_table(String dataTable, io.cucumber.datatable.DataTable dataTable1) {
                    // Write code here that turns the phrase above into concrete actions
                    // For automatic transformation, change DataTable to one of
                    // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or
                    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,
                    // Double, Byte, Short, Long, BigInteger or BigDecimal.
                    //
                    // For other transformations you can register a DataTableType.
                    throw new io.cucumber.java.PendingException();
                }
                @Given("I have in table {string}:")
                public void i_have_in_table(String string, io.cucumber.datatable.DataTable dataTable) {
                    // Write code here that turns the phrase above into concrete actions
                    // For automatic transformation, change DataTable to one of
                    // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or
                    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,
                    // Double, Byte, Short, Long, BigInteger or BigDecimal.
                    //
                    // For other transformations you can register a DataTableType.
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetForDataTable("I have in table \"M6\":", customParameterType), is(equalTo(expected)));
    }

    private String snippetForDataTable(String stepText, ParameterType<String> parameterType) {
        Step step = createStepWithDataTable(stepText);
        ParameterTypeRegistry parameterTypeRegistry = new ParameterTypeRegistry(Locale.ENGLISH);
        parameterTypeRegistry.defineParameterType(parameterType);
        List<String> snippet = new SnippetGenerator("en", new JavaSnippet(), parameterTypeRegistry).getSnippet(step,
            snippetType);
        return String.join("\n", snippet);
    }

    @Test
    void generateSnippetWithOutlineParam() {
        String expected = """
                @Given("Then it responds <param>")
                public void then_it_responds_param() {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";

        assertThat(snippetFor("Then it responds <param>"), is(equalTo(expected)));
    }

    @Test
    void generatesSnippetUsingFirstGivenWhenThenKeyWord() {
        String expected = """
                @When("I have {int} cukes in my {string} belly")
                public void i_have_cukes_in_my_belly(Integer int1, String string) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetForWhenAnd("I have 4 cukes in my \"big\" belly"), is(equalTo(expected)));
    }

    private String snippetForWhenAnd(String stepText) {
        String source = """
                Feature: Test feature
                  Scenario: Test Scenario
                    When some other step
                    And %s
                """.formatted(stepText);

        Feature feature = TestFeatureParser.parse(source);
        Step step = feature.getPickles().get(0).getSteps().get(1);
        List<String> snippet = new SnippetGenerator("en", new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH))
                .getSnippet(step, snippetType);
        return String.join("\n", snippet);
    }

    @Test
    void generatesSnippetDefaultsToGiven() {
        String expected = """
                @Given("I have {int} cukes in my {string} belly")
                public void i_have_cukes_in_my_belly(Integer int1, String string) {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        assertThat(snippetForWildCard("I have 4 cukes in my \"big\" belly"), is(equalTo(expected)));
    }

    private String snippetForWildCard(String stepText) {
        String source = """
                Feature: Test feature
                  Scenario: Test Scenario
                    * %s
                """.formatted(stepText);
        Feature feature = TestFeatureParser.parse(source);
        Step step = feature.getPickles().get(0).getSteps().get(0);
        List<String> snippet = new SnippetGenerator("en", new JavaSnippet(), new ParameterTypeRegistry(Locale.ENGLISH))
                .getSnippet(step, snippetType);
        return String.join("\n", snippet);
    }

    @Test
    void generatesEmojiSnippet() {
        String expected = """
                @NeutralFace("\uD83C\uDFB8")
                public void step_without_java_identifiers() {
                    // Write code here that turns the phrase above into concrete actions
                    throw new io.cucumber.java.PendingException();
                }""";
        String source = """
                # language: em
                \uD83D\uDCDA: \uD83D\uDE48\uD83D\uDE49\uD83D\uDE4A

                  \uD83D\uDCD5: \uD83D\uDC83
                    \uD83D\uDE10\uD83C\uDFB8
                """;

        Feature feature = TestFeatureParser.parse(source);
        Step step = feature.getPickles().get(0).getSteps().get(0);
        String language = "em";
        ParameterTypeRegistry registry = new ParameterTypeRegistry(new Locale(language));
        JavaSnippet snippet = new JavaSnippet();
        SnippetGenerator generator = new SnippetGenerator(language, snippet, registry);
        List<String> snippets = generator.getSnippet(step, snippetType);
        assertThat(String.join("\n", snippets), is(equalTo(expected)));
    }

    private static final class Size {
        // Dummy. Makes the test readable
    }

}
