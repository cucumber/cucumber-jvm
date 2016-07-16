package cucumber.runtime.java;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class Java8SnippetTest {
    private static final String GIVEN_KEYWORD = "Given";

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
                "Given(\"^I have (\\\\d+) cukes in my \\\"([^\\\"]*)\\\" belly$\", (Integer arg1, String arg2) -> {\n" +
                "    // Write code here that turns the phrase above into concrete actions\n" +
                "    throw new PendingException();\n" +
                "});\n";
        System.out.println(expected);
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    private String snippetFor(String name) {
        PickleStep step = new PickleStep(name, Collections.<Argument>emptyList(), Collections.<PickleLocation>emptyList());
        return new SnippetGenerator(new Java8Snippet()).getSnippet(step, GIVEN_KEYWORD, null);
    }
}