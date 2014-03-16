package cucumber.runtime.ioke;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class IokeSnippetTest {
    @Test
    public void generatesPlainSnippet() {
        Step step = new Step(Collections.<Comment>emptyList(), "Given ", "I have 4 cukes in my \"big\" belly", 0, null, null);
        String snippet = new SnippetGenerator(new IokeSnippet()).getSnippet(step, null);
        String expected = "" +
                "Given(#/^I have ({arg1}\\d+) cukes in my \"({arg2}.*?)\" belly$/,\n" +
                "  # Write code here that turns the phrase above into concrete actions\n" +
                ")\n";
        assertEquals(expected, snippet);
    }
}
