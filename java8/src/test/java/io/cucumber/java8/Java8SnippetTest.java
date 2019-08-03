package io.cucumber.java8;

import gherkin.pickles.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class Java8SnippetTest {

    private static final String GIVEN_KEYWORD = "Given";

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
            "Given(\"I have {int} cukes in my {string} belly\", (Integer int1, String string) -> {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new io.cucumber.java8.PendingException();\n" +
            "});\n";
        System.out.println(expected);
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    private String snippetFor(String name) {
        PickleStep step = new PickleStep(name, Collections.<Argument>emptyList(), Collections.<PickleLocation>emptyList());
        return String.join(
            "\n",
            new SnippetGenerator(
                new Java8Snippet(),
                new ParameterTypeRegistry(Locale.ENGLISH)).getSnippet(step, GIVEN_KEYWORD, SnippetType.UNDERSCORE)
        );
    }

}
