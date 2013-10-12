package cucumber.runtime.jython;

import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import cucumber.runtime.snippets.UnderscoreConcatenator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JythonSnippetTest {

    @Test
    public void generatesSnippetWithTwoArgs() {
        String expected = "" +
                "@Given('^I have (\\d+) cukes in my \"([^\"]*)\" belly$')\n" +
                "def i_have_cukes_in_my_belly(self, arg1, arg2):\n" +
                "  # Express the Regexp above with the code you wish you had\n" +
                "  raise(PendingException())\n" +
                "";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    @Test
    public void generatesSnippetWithZeroArgs() {
        String expected = "" +
                "@Given('^I have no cukes belly$')\n" +
                "def i_have_no_cukes_belly(self):\n" +
                "  # Express the Regexp above with the code you wish you had\n" +
                "  raise(PendingException())\n" +
                "";
        assertEquals(expected, snippetFor("I have no cukes belly"));
    }

    private String snippetFor(String name) {
        Step step = new Step(Collections.<Comment>emptyList(), "Given ", name, 0, null, null);
        return new SnippetGenerator(new JythonSnippet()).getSnippet(step, new FunctionNameGenerator(new UnderscoreConcatenator()));
    }
}