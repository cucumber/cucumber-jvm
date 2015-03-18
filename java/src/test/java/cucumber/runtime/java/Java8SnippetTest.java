package cucumber.runtime.java;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Java8SnippetTest {

    private static final List<Comment> NO_COMMENTS = Collections.emptyList();

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
        Step step = new Step(NO_COMMENTS, "Given ", name, 0, null, null);
        return new SnippetGenerator(new Java8Snippet()).getSnippet(step, null);
    }
}