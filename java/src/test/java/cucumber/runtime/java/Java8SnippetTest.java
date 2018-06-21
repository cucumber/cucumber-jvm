package cucumber.runtime.java;

import cucumber.messages.Pickles.PickleStep;
import cucumber.runtime.snippets.SnippetGenerator;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import org.junit.Test;

import java.util.Locale;

import static cucumber.runtime.PickleHelper.step;
import static org.junit.Assert.assertEquals;

public class Java8SnippetTest {
    private static final String GIVEN_KEYWORD = "Given";

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
            "Given(\"I have {int} cukes in my {string} belly\", (Integer int1, String string) -> {\n" +
            "    // Write code here that turns the phrase above into concrete actions\n" +
            "    throw new cucumber.api.PendingException();\n" +
            "});\n";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    private String snippetFor(String name) {
        PickleStep step = step(name);
        return new SnippetGenerator(new Java8Snippet(), new ParameterTypeRegistry(Locale.ENGLISH)).getSnippet(step, GIVEN_KEYWORD, null);
    }
}