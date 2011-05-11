package cucumber.runtime.ioke;

import gherkin.model.Comment;
import gherkin.model.Step;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class IokeSnippetGeneratorTest {
    @Test
    public void generatesPlainSnippet() {
        Step step = new Step(Collections.<Comment>emptyList(), "Given ", "I have 4 cukes in my \"big\" belly", 0);
        String snippet = new IokeSnippetGenerator(step).getSnippet();
        String expected = "" +
                "Given(#/^I have ({arg1}\\d+) cukes in my \"({arg2}[^\"]*)\" belly$/,\n" +
                "  # Express the Regexp above with the code you wish you had\n" +
                ")\n";
        assertEquals(expected, snippet);
    }
}
