package cucumber.runtime.jython;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JythonSnippetTest {

    @Test
    public void generatesPlainSnippet() {
        String expected = "" +
                "@Given('^I have (\\d+) cukes in my \"([^\"]*)\" belly$')\n" +
                "def I_have_cukes_in_my_belly(arg1, arg2):\n" +
                "  # Express the Regexp above with the code you wish you had\n" +
                "  raise(PendingException())\n" +
                "";
        assertEquals(expected, snippetFor("I have 4 cukes in my \"big\" belly"));
    }

    private String snippetFor(String name) {
        Step step = new Step(Collections.<Comment>emptyList(), "Given ", name, 0, null, null);
        return new SnippetGenerator(new JythonSnippet()).getSnippet(step);
    }
}