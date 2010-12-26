package cucumber.runtime.java;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JavaSnippetGeneratorTest {

    @Test
    public void generatesPlainSnippet() {
        Step step = new Step(Collections.<Comment>emptyList(), "Given ", "I have 4 cukes in my \"big\" belly", 0);
        String snippet = new JavaSnippetGenerator(step).getSnippet();
        String expected = "" +
                "@Given(\"^I have (\\d+) cukes in my \"([^\"]*)\" belly$\")\n" +
                "public void I_have_cukes_in_my_belly(int arg1, String arg2) {\n" +
                "    // Express the Regexp above with the code you wish you had\n" +
                "}\n";
        assertEquals(expected, snippet);
    }
}
