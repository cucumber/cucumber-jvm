package cucumber.runtime.clojure;

import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ClojureSnippetTest {
    @Test
    public void generatesPlainSnippet() {
        Step step = new Step(Collections.<Comment>emptyList(), "Given ", "I have 4 cukes in my \"big\" belly", 0, null, null);
        String snippet = new SnippetGenerator(new ClojureSnippet()).getSnippet(step);
        String expected = "" +
                "(Given #\"^I have (\\d+) cukes in my \"([^\"]*)\" belly$\" [arg1, arg2]\n" +
<<<<<<< HEAD
                "  (comment  Express the Regexp above with the code you wish you had  ))\n";
        assertEquals(expected, snippet);
    }
}
